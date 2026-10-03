package com.apr.community.domain.post.controller;

import com.apr.community.domain.post.dto.PostRequestDTO;
import com.apr.community.domain.post.dto.PostResponseDTO;
import com.apr.community.domain.post.service.PostCommandService;
import com.apr.community.domain.post.service.PostQueryService;
import com.apr.community.global.pagination.PageResponse;
import com.apr.community.global.requester.RequesterId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostQueryService postQueryService;
    private final PostCommandService postCommandService;

    @GetMapping
    public PageResponse<PostResponseDTO.PostDTO> getPosts(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @RequesterId String requester
    ) {
        return postQueryService.getPosts(cursor, size, requester);
    }

    @GetMapping("/{postId}")
    public PostResponseDTO.PostDTO getPost(
            @PathVariable Long postId,
            @RequesterId String requester
    ) {
        return postQueryService.getPost(postId, requester);
    }

    @PostMapping
    public ResponseEntity<PostResponseDTO.PostDTO> createPost(
            @Valid @RequestBody PostRequestDTO.PostDTO request,
            @RequesterId String requester
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postCommandService.createPost(request, requester));
    }

    @PatchMapping("/{postId}")
    public PostResponseDTO.PostDTO updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostRequestDTO.PostDTO request,
            @RequesterId String requester
    ) {
        return postCommandService.updatePost(postId, request, requester);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @RequesterId String requester
    ) {
        postCommandService.deletePost(postId, requester);
        return ResponseEntity.noContent().build();
    }
}
