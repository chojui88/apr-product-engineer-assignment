package com.apr.community.domain.comment.converter;

import com.apr.community.domain.comment.dto.CommentRequestDTO;
import com.apr.community.domain.comment.dto.CommentResponseDTO;
import com.apr.community.domain.comment.entity.Comment;

public class CommentConverter {

    // DTO -> Comment 엔티티
    public static Comment toComment(Long postId, CommentRequestDTO.CommentDTO request, String author) {
        return Comment.createComment(postId, request.content(), author);
    }

    // entity -> 댓글 DTO
    public static CommentResponseDTO.CommentDTO toCommentDTO(Comment comment) {
        return CommentResponseDTO.CommentDTO.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .content(comment.getContent())
                .author(comment.getAuthor())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
