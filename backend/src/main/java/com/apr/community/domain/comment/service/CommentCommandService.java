package com.apr.community.domain.comment.service;

import com.apr.community.domain.comment.converter.CommentConverter;
import com.apr.community.domain.comment.dto.CommentRequestDTO;
import com.apr.community.domain.comment.dto.CommentResponseDTO;
import com.apr.community.domain.comment.entity.Comment;
import com.apr.community.domain.comment.exception.CommentErrorCode;
import com.apr.community.domain.comment.exception.CommentException;
import com.apr.community.domain.comment.repository.CommentRepository;
import com.apr.community.domain.post.exception.PostErrorCode;
import com.apr.community.domain.post.exception.PostException;
import com.apr.community.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentResponseDTO.CommentDTO createComment(Long postId, CommentRequestDTO.CommentDTO request, String requester) {
        // 글이 없거나 삭제됐으면 404
        if (!postRepository.existsByIdAndDeletedAtIsNull(postId)) {
            throw new PostException(PostErrorCode.POST_NOT_FOUND);
        }

        // content trim 후 필수 (@NotBlank는 trim() 기준이라, 전각 공백(U+3000) 등은 여기서 한 번 더 걸러낸다)
        String content = request.content().strip();
        if (content.isBlank()) {
            throw new CommentException(CommentErrorCode.BLANK_CONTENT);
        }

        Comment comment = Comment.createComment(postId, content, requester);
        Comment savedComment = commentRepository.save(comment);

        return CommentConverter.toCommentDTO(savedComment);
    }

    public void deleteComment(Long commentId, String requester) {
        // 댓글이 없거나 이미 삭제됐으면 404
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));

        // 부모 글이 삭제됐으면 404 (삭제된 글에 달린 댓글은 더 조작할 수 없음)
        if (!postRepository.existsByIdAndDeletedAtIsNull(comment.getPostId())) {
            throw new CommentException(CommentErrorCode.COMMENT_NOT_FOUND);
        }

        // 본인 댓글만 삭제 가능
        if (!comment.isOwnedBy(requester)) {
            throw new CommentException(CommentErrorCode.COMMENT_FORBIDDEN);
        }

        comment.softDelete();
    }
}
