package com.apr.community.domain.post.service;

import com.apr.community.domain.comment.repository.CommentRepository;
import com.apr.community.domain.like.entity.PostLikeId;
import com.apr.community.domain.like.repository.PostLikeRepository;
import com.apr.community.domain.post.dto.PostRequestDTO;
import com.apr.community.domain.post.dto.PostResponseDTO;
import com.apr.community.domain.post.entity.Post;
import com.apr.community.domain.post.exception.PostErrorCode;
import com.apr.community.domain.post.exception.PostException;
import com.apr.community.domain.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class PostCommandServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PostLikeRepository postLikeRepository;

    private PostCommandService service;

    @BeforeEach
    void setUp() {
        service = new PostCommandService(postRepository, commentRepository, postLikeRepository);
    }

    private static Post postOf(Long id, String author) {
        Post post = Post.createPost("원래 제목", "원래 내용", author);
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    @Test
    void createPost_성공하면_댓글수0_좋아요안함으로_응답() {
        PostRequestDTO.PostDTO request = new PostRequestDTO.PostDTO("  제목  ", "내용");
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        PostResponseDTO.PostDTO result = service.createPost(request, "Jelin");

        assertThat(result.title()).isEqualTo("제목"); // 앞뒤 공백 제거
        assertThat(result.content()).isEqualTo("내용");
        assertThat(result.author()).isEqualTo("Jelin");
        assertThat(result.commentCount()).isZero();
        assertThat(result.isLiked()).isFalse();
    }

    @Test
    void createPost_제목이_20자_초과면_예외() {
        PostRequestDTO.PostDTO request = new PostRequestDTO.PostDTO("1".repeat(21), "내용");

        assertThatThrownBy(() -> service.createPost(request, "Jelin"))
                .isInstanceOf(PostException.class)
                .extracting(e -> ((PostException) e).getBaseErrorCode())
                .isEqualTo(PostErrorCode.INVALID_TITLE_LENGTH);

        verify(postRepository, never()).save(any());
    }

    @Test
    void createPost_앞뒤_공백은_길이_계산에서_제외() {
        PostRequestDTO.PostDTO request = new PostRequestDTO.PostDTO(" " + "1".repeat(20) + " ", "내용");
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        PostResponseDTO.PostDTO result = service.createPost(request, "Jelin");

        assertThat(result.title()).isEqualTo("1".repeat(20));
    }

    @Test
    void createPost_이모지_20개는_허용() {
        PostRequestDTO.PostDTO request = new PostRequestDTO.PostDTO("😀".repeat(20), "내용");
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        PostResponseDTO.PostDTO result = service.createPost(request, "Jelin");

        assertThat(result.title()).isEqualTo("😀".repeat(20));
    }

    @Test
    void updatePost_게시글_없으면_404() {
        when(postRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.empty());
        PostRequestDTO.PostDTO request = new PostRequestDTO.PostDTO("제목", "내용");

        assertThatThrownBy(() -> service.updatePost(1L, request, "Jelin"))
                .isInstanceOf(PostException.class)
                .extracting(e -> ((PostException) e).getBaseErrorCode())
                .isEqualTo(PostErrorCode.POST_NOT_FOUND);
    }

    @Test
    void updatePost_본인_글이_아니면_403() {
        Post post = postOf(1L, "다른사람");
        when(postRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.of(post));
        PostRequestDTO.PostDTO request = new PostRequestDTO.PostDTO("제목", "내용");

        assertThatThrownBy(() -> service.updatePost(1L, request, "Jelin"))
                .isInstanceOf(PostException.class)
                .extracting(e -> ((PostException) e).getBaseErrorCode())
                .isEqualTo(PostErrorCode.POST_FORBIDDEN);
    }

    @Test
    void updatePost_제목이_20자_초과면_400() {
        Post post = postOf(1L, "Jelin");
        when(postRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.of(post));
        PostRequestDTO.PostDTO request = new PostRequestDTO.PostDTO("1".repeat(21), "내용");

        assertThatThrownBy(() -> service.updatePost(1L, request, "Jelin"))
                .isInstanceOf(PostException.class)
                .extracting(e -> ((PostException) e).getBaseErrorCode())
                .isEqualTo(PostErrorCode.INVALID_TITLE_LENGTH);
    }

    @Test
    void updatePost_성공하면_댓글수와_좋아요여부_채워서_응답() {
        Post post = postOf(1L, "Jelin");
        when(postRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.of(post));
        when(commentRepository.countByPostIdAndDeletedAtIsNull(1L)).thenReturn(3L);
        when(postLikeRepository.existsById(new PostLikeId(1L, "Jelin"))).thenReturn(true);
        PostRequestDTO.PostDTO request = new PostRequestDTO.PostDTO("새 제목", "새 내용");

        PostResponseDTO.PostDTO result = service.updatePost(1L, request, "Jelin");

        assertThat(result.title()).isEqualTo("새 제목");
        assertThat(result.content()).isEqualTo("새 내용");
        assertThat(result.commentCount()).isEqualTo(3L);
        assertThat(result.isLiked()).isTrue();
    }

    @Test
    void deletePost_게시글_없으면_404() {
        when(postRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deletePost(1L, "Jelin"))
                .isInstanceOf(PostException.class)
                .extracting(e -> ((PostException) e).getBaseErrorCode())
                .isEqualTo(PostErrorCode.POST_NOT_FOUND);
    }

    @Test
    void deletePost_본인_글이_아니면_403_그리고_삭제되지_않음() {
        Post post = postOf(1L, "다른사람");
        when(postRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> service.deletePost(1L, "Jelin"))
                .isInstanceOf(PostException.class)
                .extracting(e -> ((PostException) e).getBaseErrorCode())
                .isEqualTo(PostErrorCode.POST_FORBIDDEN);

        assertThat(post.isDeleted()).isFalse();
    }

    @Test
    void deletePost_성공하면_softDelete() {
        Post post = postOf(1L, "Jelin");
        when(postRepository.findActiveByIdForUpdate(1L)).thenReturn(Optional.of(post));

        service.deletePost(1L, "Jelin");

        assertThat(post.isDeleted()).isTrue();
    }
}
