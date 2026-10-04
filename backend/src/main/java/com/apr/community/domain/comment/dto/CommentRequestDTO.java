package com.apr.community.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentRequestDTO {

    // 댓글 작성
    public record CommentDTO(
            @NotBlank(message = "댓글 내용은 필수 입력값입니다.")
            String content
    ) {}
}
