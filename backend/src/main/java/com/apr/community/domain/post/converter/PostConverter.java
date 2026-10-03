package com.apr.community.domain.post.converter;

import com.apr.community.domain.post.dto.PostRequestDTO;
import com.apr.community.domain.post.dto.PostResponseDTO;
import com.apr.community.domain.post.entity.Post;

public class PostConverter {

    // DTO -> Post 엔티티
    public static Post toPost(PostRequestDTO.PostDTO request, String author) {
        return Post.createPost(request.title(), request.content(), author);
    }

    // entity -> 게시글 DTO (commentCount · isLiked 는 요청자 기준으로 따로 계산해서 전달)
    public static PostResponseDTO.PostDTO toPostDTO(Post post, long commentCount, boolean isLiked) {
        return PostResponseDTO.PostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .likeCount(post.getLikeCount())
                .commentCount(commentCount)
                .isLiked(isLiked)
                .author(post.getAuthor())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
