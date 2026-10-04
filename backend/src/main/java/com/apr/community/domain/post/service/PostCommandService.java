package com.apr.community.domain.post.service;

import com.apr.community.domain.comment.repository.CommentRepository;
import com.apr.community.domain.like.entity.PostLikeId;
import com.apr.community.domain.like.repository.PostLikeRepository;
import com.apr.community.domain.post.converter.PostConverter;
import com.apr.community.domain.post.dto.PostRequestDTO;
import com.apr.community.domain.post.dto.PostResponseDTO;
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
public class PostCommandService {

    static final int TITLE_MAX_CODE_POINTS = 20;

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;

    public PostResponseDTO.PostDTO createPost(PostRequestDTO.PostDTO request, String requester) {
        String title = request.title().strip();

        int titleLength = title.codePointCount(0, title.length());
        if (titleLength > TITLE_MAX_CODE_POINTS) {
            throw new PostException(PostErrorCode.INVALID_TITLE_LENGTH);
        }

        Post post = Post.createPost(title, request.content(), requester);
        Post savedPost = postRepository.save(post);

        return PostConverter.toPostDTO(savedPost, 0L, false);
    }

    // 수정 가능한 게시글, 작성자가 본인이면 수정하고 응답 DTO 반환
    public PostResponseDTO.PostDTO updatePost(Long postId, PostRequestDTO.PostDTO request, String requester) {
        // ID로 삭제되지 않은 게시글 조회 -> 삭제되었으면 404
        Post post = postRepository.findActiveByIdForUpdate(postId)
                .orElseThrow(() ->
                        new PostException(PostErrorCode.POST_NOT_FOUND) );

        // 작성자 본인인지 확인 -> 본인이 아니면 403
        if (!post.isOwnedBy(requester)) {
            throw new PostException(PostErrorCode.POST_FORBIDDEN);
        }
        // 제목을 trim/strip 하고 20자 제한 확인 -> 잘못됐으면 400
        String title = request.title().strip();
        if (title.codePointCount(0, title.length()) > TITLE_MAX_CODE_POINTS) {
            throw new PostException(PostErrorCode.INVALID_TITLE_LENGTH);
        }

        post.update(title, request.content());
        // 엔티티 update 노출 -> 제목, 내용, updatedAt

        // 수정한 게시글의 응답 정보 구성 +댓글, 좋아요
        long commentCount = commentRepository.countByPostIdAndDeletedAtIsNull(postId);
        boolean isLiked = postLikeRepository.existsById(new PostLikeId(postId, requester));

        // 게시글 DTO 반환
        return  PostConverter.toPostDTO(post, commentCount, isLiked);
    }

    public void deletePost(Long postId, String requester) {
        Post post = postRepository.findActiveByIdForUpdate(postId)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        if (!post.isOwnedBy(requester)) {
            throw new PostException(PostErrorCode.POST_FORBIDDEN);
        }

        post.softDelete();
    }
}
