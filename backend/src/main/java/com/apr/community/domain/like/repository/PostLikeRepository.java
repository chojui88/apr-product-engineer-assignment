package com.apr.community.domain.like.repository;

import com.apr.community.domain.like.entity.PostLike;
import com.apr.community.domain.like.entity.PostLikeId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {

    @Query("""
        select l.id.postId from PostLike l
        WHERE l.id.userId = :userId and l.id.postId in :postIds
        """)
    List<Long> findLikedPostIds(@Param("userId") String userId,
                                @Param("postIds")Collection<Long> postIds);
}
