package com.apr.community.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.Instant;

public class PostResponseDTO {

    // 목록 items 항목 = 상세 = 작성·수정 응답 (같은 형태)
    @Builder
    public record PostDTO(
            Long id,
            String title,
            String content,
            int likeCount,
            long commentCount,
            @JsonProperty("isLiked") boolean isLiked,
            String author,
            Instant createdAt
    ) {}
}
