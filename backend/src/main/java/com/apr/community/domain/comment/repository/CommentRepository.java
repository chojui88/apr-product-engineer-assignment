package com.apr.community.domain.comment.repository;

import com.apr.community.domain.comment.entity.Comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 댓글 수와 좋아요 여부
    interface PostCommentCount {
        Long getPostId();
        long getCnt();
    }

    @Query("""
        SELECT c.postId as postId, count(c) as cnt
        FROM Comment c
        WHERE c.postId in :postIds and c.deletedAt is null
        GROUP BY c.postId
        """)
    List<PostCommentCount> countByPostIds(@Param("postIds")Collection<Long> postIds);

    // 삭제 안 된 댓글 단건 조회
    Optional<Comment> findByIdAndDeletedAtIsNull(Long id);

    // 댓글 목록 첫 페이지: 최신순 (idx_comments_active 사용)
    @Query("""
        SELECT c FROM Comment c
        WHERE c.postId = :postId AND c.deletedAt IS NULL
        ORDER BY c.createdAt DESC, c.id DESC
        """)
    List<Comment> findFirstPage(@Param("postId") Long postId, Pageable pageable);

    // 댓글 목록 다음 페이지: 커서 (createdAt, id) 이전 항목
    @Query("""
        SELECT c FROM Comment c
        WHERE c.postId = :postId AND c.deletedAt IS NULL
          AND (c.createdAt < :createdAt
               OR (c.createdAt = :createdAt AND c.id < :id))
        ORDER BY c.createdAt DESC, c.id DESC
        """)
    List<Comment> findNextPage(@Param("postId") Long postId,
                               @Param("createdAt") Instant createdAt,
                               @Param("id") Long id,
                               Pageable pageable);

    // 삭제 제외 댓글 수 (상세 commentCount, 댓글 목록 total)
    long countByPostIdAndDeletedAtIsNull(Long postId);
}
