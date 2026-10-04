package com.apr.community.domain.comment.repository;

import com.apr.community.domain.comment.entity.Comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

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

    //
    // TODO(#6) 삭제 안 된 댓글 단건 조회
    // TODO(#6) 커서 목록: post_id = :postId AND (created_at, id) < (...) ORDER BY created_at DESC, id DESC
    // 단건용 댓글 수
    long countByPostIdAndDeletedAtIsNull(Long postId);
}
