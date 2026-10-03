package com.apr.community.domain.comment.service;

import com.apr.community.domain.comment.dto.CommentRequestDTO;
import com.apr.community.domain.comment.dto.CommentResponseDTO;
import com.apr.community.domain.comment.repository.CommentRepository;
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
        // TODO(#6) content trim 후 필수 (CommentErrorCode.BLANK_CONTENT)
        throw new UnsupportedOperationException("TODO(#6)");
    }

    public void deleteComment(Long commentId, String requester) {
        // TODO(#6) 본인 댓글만(COMMENT_FORBIDDEN), soft delete, 부모 글이 삭제됐으면 404
        throw new UnsupportedOperationException("TODO(#6)");
    }
}
