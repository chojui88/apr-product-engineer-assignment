package com.apr.community.domain.comment.repository;

import com.apr.community.domain.comment.entity.Comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // TODO(#6) 삭제 안 된 댓글 단건 조회
    // TODO(#6) 커서 목록: post_id = :postId AND (created_at, id) < (...) ORDER BY created_at DESC, id DESC
    // TODO(#6) total: countByPostIdAndDeletedAtIsNull
    // TODO(#4) 목록 commentCount: post_id IN (...) GROUP BY post_id (N+1 방지)
}
