package com.apr.community.domain.post.dto;

import jakarta.validation.constraints.NotBlank;

public class PostRequestDTO {

    // 게시글 작성·수정 공용 (제약 동일). 제목 20자 제한은 코드포인트 기준이라 서비스에서 검증
    public record PostDTO(
            @NotBlank(message = "제목은 필수 입력값입니다.")
            String title,

            @NotBlank(message = "내용은 필수 입력값입니다.")
            String content
    ) {}
}
