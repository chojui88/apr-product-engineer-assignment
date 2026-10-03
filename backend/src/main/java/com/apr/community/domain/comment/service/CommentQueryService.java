package com.apr.community.domain.comment.service;

import com.apr.community.domain.comment.dto.CommentResponseDTO;
import com.apr.community.domain.comment.repository.CommentRepository;
import com.apr.community.domain.post.repository.PostRepository;
import com.apr.community.global.pagination.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public PageResponse<CommentResponseDTO.CommentDTO> getComments(Long postId, String cursor, int size) {
        // TODO(#6) 삭제된 글이면 PostErrorCode.POST_NOT_FOUND, 최신순 커서 목록
        throw new UnsupportedOperationException("TODO(#6)");
    }
}
