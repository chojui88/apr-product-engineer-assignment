package com.apr.community.domain.like.repository;

import com.apr.community.domain.like.entity.PostLike;
import com.apr.community.domain.like.entity.PostLikeId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {

    // TODO(#4) 목록 isLiked: user_id = :requester AND post_id IN (...) 인 postId 목록
}
