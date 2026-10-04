package com.apr.community.domain.post.repository;

import com.apr.community.domain.post.entity.Post;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 삭제 안 된 글 단건 조회: findByIdAndDeletedAtIsNull
    Optional<Post> findByIdAndDeletedAtIsNull(Long id);

    //삭제 제외한 전체 개수
    long countByDeletedAtIsNull();

    //  커서 목록: (created_at, id) < (:cursorCreatedAt, :cursorId) ORDER BY created_at DESC, id DESC LIMIT :size
    @Query("""
        SELECT p FROM Post p
        WHERE p.deletedAt IS NULL
        ORDER BY p.createdAt DESC, p.id DESC
    """)
    List<Post> findFirstPage(Pageable pageable);

    // 목록 다음 페이지
    @Query("""
        SELECT p FROM Post p
        WHERE p.deletedAt IS NULL 
        AND(p.createdAt < :createdAt
                OR (p.createdAt = :createdAt AND p.id < :id))
        ORDER BY p.createdAt DESC, p.id DESC
               """)
    List<Post> findNextPage(@Param("createdAt")Instant createdAt,
                            @Param("id") Long id,
                            Pageable pageable);

    // TODO(#5) like_count ±1 원자적 UPDATE (@Modifying)
}
