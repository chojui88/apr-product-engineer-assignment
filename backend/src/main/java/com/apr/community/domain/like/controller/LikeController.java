package com.apr.community.domain.like.controller;

import com.apr.community.domain.like.dto.LikeResponseDTO;
import com.apr.community.domain.like.service.LikeCommandService;
import com.apr.community.global.requester.RequesterId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/likes")
public class LikeController {

    private final LikeCommandService likeCommandService;

    @PostMapping("/toggle")
    public LikeResponseDTO.LikeToggleDTO toggle(
            @PathVariable Long postId,
            @RequesterId String requester
    ) {
        return likeCommandService.toggle(postId, requester);
    }
}
