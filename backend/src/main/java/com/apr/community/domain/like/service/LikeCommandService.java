package com.apr.community.domain.like.service;

import com.apr.community.domain.like.dto.LikeResponseDTO;
import com.apr.community.domain.like.repository.PostLikeRepository;
import com.apr.community.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeCommandService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    public LikeResponseDTO.LikeToggleDTO toggle(Long postId, String requester) {
        // TODO(#5) 삭제된 글이면 PostErrorCode.POST_NOT_FOUND
        // TODO(#5) post_likes insert/delete + like_count ±1 을 한 트랜잭션에서
        throw new UnsupportedOperationException("TODO(#5)");
    }
}
