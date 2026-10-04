package com.apr.community.domain.post.service;

import com.apr.community.domain.post.dto.PostRequestDTO;
import com.apr.community.domain.post.dto.PostResponseDTO;
import com.apr.community.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostCommandService {

    static final int TITLE_MAX_CODE_POINTS = 20;

    private final PostRepository postRepository;

    public PostResponseDTO.PostDTO createPost(PostRequestDTO.PostDTO request, String requester) {
        // TODO(#4) trim → 제목 codePointCount ≤ 20 검증 (초과 시 PostErrorCode.INVALID_TITLE_LENGTH)
        throw new UnsupportedOperationException("TODO(#4)");
    }

    public PostResponseDTO.PostDTO updatePost(Long postId, PostRequestDTO.PostDTO request, String requester) {
        // TODO(#4) 본인 글 아니면 PostErrorCode.POST_FORBIDDEN
        throw new UnsupportedOperationException("TODO(#4)");
    }

    public void deletePost(Long postId, String requester) {
        // TODO(#4) 본인 글만, soft delete
        throw new UnsupportedOperationException("TODO(#4)");
    }
}
