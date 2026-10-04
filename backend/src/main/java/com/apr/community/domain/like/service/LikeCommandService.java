package com.apr.community.domain.like.service;

import com.apr.community.domain.like.converter.LikeConverter;
import com.apr.community.domain.like.dto.LikeResponseDTO;
import com.apr.community.domain.like.entity.PostLikeId;
import com.apr.community.domain.like.repository.PostLikeRepository;
import com.apr.community.domain.post.entity.Post;
import com.apr.community.domain.post.exception.PostErrorCode;
import com.apr.community.domain.post.exception.PostException;
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
        // 삭제된 글이면 PostErrorCode.POST_NOT_FOUND
        Post post = postRepository.findActiveByIdForUpdate(postId)
                .orElseThrow(()->
                        new PostException(PostErrorCode.POST_NOT_FOUND));
        //  post_likes insert/delete + like_count ±1 을 한 트랜잭션에서
        PostLikeId likeId = new PostLikeId(postId, requester);
        boolean isLiked = postLikeRepository.existsById(likeId); //기록 확인

        if (isLiked) { // 좋아요 되어 있음
            postLikeRepository.deleteById(likeId);
            post.decreaseLikeCount();

            return LikeConverter.toLikeToggleDTO(false, post.getLikeCount());
        }

        postLikeRepository.save(LikeConverter.toPostLike(postId, requester));
        post.increaseLikeCount();

        return LikeConverter.toLikeToggleDTO(true, post.getLikeCount());
    }
}
