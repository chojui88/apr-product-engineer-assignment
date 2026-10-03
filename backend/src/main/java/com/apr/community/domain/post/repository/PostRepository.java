package com.apr.community.domain.post.repository;

import com.apr.community.domain.post.entity.Post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // TODO(#4) 삭제 안 된 글 단건 조회: findByIdAndDeletedAtIsNull
    // TODO(#4) 커서 목록: (created_at, id) < (:cursorCreatedAt, :cursorId) ORDER BY created_at DESC, id DESC LIMIT :size
    // TODO(#4) total: countByDeletedAtIsNull
    // TODO(#5) like_count ±1 원자적 UPDATE (@Modifying)
}
