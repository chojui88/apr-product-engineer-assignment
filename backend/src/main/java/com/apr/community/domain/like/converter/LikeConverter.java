package com.apr.community.domain.like.converter;

import com.apr.community.domain.like.dto.LikeResponseDTO;
import com.apr.community.domain.like.entity.PostLike;

public class LikeConverter {

    // 요청자 + 게시글 -> PostLike 엔티티
    public static PostLike toPostLike(Long postId, String userId) {
        return PostLike.createPostLike(postId, userId);
    }

    // 토글 결과 -> 좋아요 토글 DTO
    public static LikeResponseDTO.LikeToggleDTO toLikeToggleDTO(boolean isLiked, int likeCount) {
        return LikeResponseDTO.LikeToggleDTO.builder()
                .isLiked(isLiked)
                .likeCount(likeCount)
                .build();
    }
}
