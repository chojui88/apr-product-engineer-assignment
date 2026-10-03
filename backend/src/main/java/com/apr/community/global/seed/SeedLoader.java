package com.apr.community.global.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 기동 시 seed.json 을 한 번만 적재한다.
 *
 * - id 를 그대로 넣어야 해서 JPA(IDENTITY) 대신 JdbcTemplate batch insert 사용
 * - 적재 여부는 seed_history 로 판단 (데이터 존재 여부와 분리)
 * - 완전히 성공하거나 명확히 실패한다. 부분 성공은 없다.
 *   (파일 없음 / 예상 못 한 id 충돌 → 기동 실패, 중간 예외 → 전부 롤백)
 */
@Slf4j
@Component
public class SeedLoader implements ApplicationRunner {

    private static final String SEED_NAME = "seed.json";

    // id 를 직접 넣으면 시퀀스는 1 그대로라 다음 INSERT 에서 PK 충돌 → MAX(id) 로 보정
    // 테이블명은 바인딩할 수 없으므로 문자열 결합 대신 테이블별 리터럴로 고정
    private static final String RESET_POSTS_SEQ = """
            SELECT setval(pg_get_serial_sequence('posts', 'id'),
                          COALESCE((SELECT MAX(id) FROM posts), 1),
                          (SELECT MAX(id) FROM posts) IS NOT NULL)
            """;

    private static final String RESET_COMMENTS_SEQ = """
            SELECT setval(pg_get_serial_sequence('comments', 'id'),
                          COALESCE((SELECT MAX(id) FROM comments), 1),
                          (SELECT MAX(id) FROM comments) IS NOT NULL)
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final String seedPath;

    public SeedLoader(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper,
                      @Value("${app.seed-path}") String seedPath) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.seedPath = seedPath;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) throws IOException {
        // 1. 이미 적재했으면 건너뜀 (파일 확인보다 먼저 → 적재 후엔 파일이 없어도 기동 가능)
        Integer applied = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM seed_history WHERE name = ?", Integer.class, SEED_NAME);
        if (applied != null && applied > 0) {
            log.info("[Seed] 이미 적재됨 → 건너뜀");
            return;
        }

        // 2. 파일 읽기 (없으면 기동 실패 → 경로 문제를 즉시 드러냄)
        Path path = Path.of(seedPath);
        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException("시드 파일을 찾을 수 없습니다: " + path.toAbsolutePath());
        }
        SeedData seed;
        try (InputStream in = Files.newInputStream(path)) {
            seed = objectMapper.readValue(in, SeedData.class);
        }

        Set<Long> deletedPostIds = new HashSet<>(seed.deletedPostIds());
        Set<Long> deletedCommentIds = new HashSet<>(seed.deletedCommentIds());

        // 삭제 시각: seed 스냅샷 시점(가장 늦은 createdAt)으로 고정
        // → 재적재해도 같은 값, 모든 작성 시각보다 같거나 늦음
        OffsetDateTime deletedAt = toUtc(Stream.concat(
                        seed.posts().stream().map(SeedData.SeedPost::createdAt),
                        seed.comments().stream().map(SeedData.SeedComment::createdAt))
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalStateException("시드에 게시글·댓글이 없습니다.")));

        // 3. posts (updated_at = created_at : seed 글은 수정 이력 없음)
        //    ON CONFLICT 를 쓰지 않음 → 예상 못 한 id 충돌은 오류로 드러내고 롤백
        jdbcTemplate.batchUpdate("""
                INSERT INTO posts (id, title, content, author, like_count, created_at, updated_at, deleted_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                seed.posts(), 100, (ps, p) -> {
                    OffsetDateTime createdAt = toUtc(p.createdAt());
                    ps.setLong(1, p.id());
                    ps.setString(2, p.title());
                    ps.setString(3, p.content());
                    ps.setString(4, p.author());
                    ps.setInt(5, p.likeCount());
                    ps.setObject(6, createdAt);
                    ps.setObject(7, createdAt);
                    ps.setObject(8, deletedPostIds.contains(p.id()) ? deletedAt : null);
                });

        // 4. comments
        jdbcTemplate.batchUpdate("""
                INSERT INTO comments (id, post_id, content, author, created_at, deleted_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                seed.comments(), 500, (ps, c) -> {
                    ps.setLong(1, c.id());
                    ps.setLong(2, c.postId());
                    ps.setString(3, c.content());
                    ps.setString(4, c.author());
                    ps.setObject(5, toUtc(c.createdAt()));
                    ps.setObject(6, deletedCommentIds.contains(c.id()) ? deletedAt : null);
                });

        // 5. 시퀀스 보정 (시퀀스 변경은 롤백되지 않지만, 재시도 시 다시 MAX(id) 로 맞추므로 결과에 영향 없음)
        Long postSeq = jdbcTemplate.queryForObject(RESET_POSTS_SEQ, Long.class);
        Long commentSeq = jdbcTemplate.queryForObject(RESET_COMMENTS_SEQ, Long.class);

        // 6. 적재 완료 기록
        jdbcTemplate.update("INSERT INTO seed_history (name) VALUES (?)", SEED_NAME);

        log.info("[Seed] 적재 완료: posts {}, comments {} (삭제 posts {}, comments {}), 시퀀스 posts → {}, comments → {}",
                seed.posts().size(), seed.comments().size(),
                deletedPostIds.size(), deletedCommentIds.size(),
                postSeq, commentSeq);
    }

    private static OffsetDateTime toUtc(Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
