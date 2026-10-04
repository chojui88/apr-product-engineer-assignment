package com.apr.community.domain.like.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

public class LikeResponseDTO {

    // 좋아요 토글 결과
    @Builder
    public record LikeToggleDTO(
            @JsonProperty("isLiked") boolean isLiked,
            int likeCount
    ) {}
}
