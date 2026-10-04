package com.apr.community.domain.like.service;

import com.apr.community.domain.like.dto.LikeResponseDTO;
import com.apr.community.domain.like.entity.PostLike;
import com.apr.community.domain.like.entity.PostLikeId;
import com.apr.community.domain.like.repository.PostLikeRepository;
import com.apr.community.domain.post.entity.Post;
import com.apr.community.domain.post.exception.PostErrorCode;
import com.apr.community.domain.post.exception.PostException;
import com.apr.community.domain.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LikeCommandServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private PostLikeRepository postLikeRepository;

    private LikeCommandService service;

    @BeforeEach
    void setUp() {
        service = new LikeCommandService(postRepository, postLikeRepository);
    }

    private static Post postOf(Long id, int likeCount) {
        Post post = Post.createPost("제목", "내용", "author");
        ReflectionTestUtils.setField(post, "id", id);
        ReflectionTestUtils.setField(post, "likeCount", likeCount);
        return post;
    }

    @Test
    void toggle_게시글_없으면_404() {
        when(postRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.toggle(1L, "Jelin"))
                .isInstanceOf(PostException.class)
                .extracting(e -> ((PostException) e).getBaseErrorCode())
                .isEqualTo(PostErrorCode.POST_NOT_FOUND);

        verify(postLikeRepository, never()).existsById(any());
    }

    @Test
    void toggle_좋아요_안한_상태면_좋아요_추가() {
        Post post = postOf(1L, 3);
        when(postRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.of(post));
        when(postLikeRepository.existsById(new PostLikeId(1L, "Jelin"))).thenReturn(false);

        LikeResponseDTO.LikeToggleDTO result = service.toggle(1L, "Jelin");

        assertThat(result.isLiked()).isTrue();
        assertThat(result.likeCount()).isEqualTo(4);
        assertThat(post.getLikeCount()).isEqualTo(4);

        ArgumentCaptor<PostLike> captor = ArgumentCaptor.forClass(PostLike.class);
        verify(postLikeRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(new PostLikeId(1L, "Jelin"));
        verify(postLikeRepository, never()).deleteById(any());
    }

    @Test
    void toggle_좋아요_한_상태면_좋아요_취소() {
        Post post = postOf(1L, 3);
        when(postRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.of(post));
        when(postLikeRepository.existsById(new PostLikeId(1L, "Jelin"))).thenReturn(true);

        LikeResponseDTO.LikeToggleDTO result = service.toggle(1L, "Jelin");

        assertThat(result.isLiked()).isFalse();
        assertThat(result.likeCount()).isEqualTo(2);
        assertThat(post.getLikeCount()).isEqualTo(2);
        verify(postLikeRepository).deleteById(new PostLikeId(1L, "Jelin"));
        verify(postLikeRepository, never()).save(any());
    }

    @Test
    void toggle_좋아요수가_0일때_취소해도_0_밑으로_내려가지_않음() {
        Post post = postOf(1L, 0);
        when(postRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.of(post));
        when(postLikeRepository.existsById(new PostLikeId(1L, "Jelin"))).thenReturn(true);

        LikeResponseDTO.LikeToggleDTO result = service.toggle(1L, "Jelin");

        assertThat(result.likeCount()).isZero();
    }
}
