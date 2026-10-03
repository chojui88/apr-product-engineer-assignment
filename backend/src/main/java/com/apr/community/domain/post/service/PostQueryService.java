package com.apr.community.domain.post.service;

import com.apr.community.domain.comment.repository.CommentRepository;
import com.apr.community.domain.like.repository.PostLikeRepository;
import com.apr.community.domain.post.dto.PostResponseDTO;
import com.apr.community.domain.post.repository.PostRepository;
import com.apr.community.global.pagination.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;

    public PageResponse<PostResponseDTO.PostDTO> getPosts(String cursor, int size, String requester) {
        // TODO(#4) 커서 목록 + total + commentCount/isLiked 일괄 조회
        throw new UnsupportedOperationException("TODO(#4)");
    }

    public PostResponseDTO.PostDTO getPost(Long postId, String requester) {
        // TODO(#4) 없거나 삭제됐으면 new PostException(PostErrorCode.POST_NOT_FOUND)
        throw new UnsupportedOperationException("TODO(#4)");
    }
}
