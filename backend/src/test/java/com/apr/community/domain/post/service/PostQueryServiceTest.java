package com.apr.community.domain.post.service;

import com.apr.community.domain.comment.repository.CommentRepository;
import com.apr.community.domain.like.entity.PostLikeId;
import com.apr.community.domain.like.repository.PostLikeRepository;
import com.apr.community.domain.post.dto.PostResponseDTO;
import com.apr.community.domain.post.entity.Post;
import com.apr.community.domain.post.exception.PostErrorCode;
import com.apr.community.domain.post.exception.PostException;
import com.apr.community.domain.post.repository.PostRepository;
import com.apr.community.global.pagination.Cursor;
import com.apr.community.global.pagination.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostQueryServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PostLikeRepository postLikeRepository;

    private PostQueryService service;

    @BeforeEach
    void setUp() {
        service = new PostQueryService(postRepository, commentRepository, postLikeRepository);
    }

    private static Post postOf(Long id, String author, Instant createdAt) {
        Post post = Post.createPost("제목", "내용", author);
        ReflectionTestUtils.setField(post, "id", id);
        ReflectionTestUtils.setField(post, "createdAt", createdAt);
        return post;
    }

    private static CommentRepository.PostCommentCount countOf(Long postId, long cnt) {
        return new CommentRepository.PostCommentCount() {
            @Override
            public Long getPostId() {
                return postId;
            }

            @Override
            public long getCnt() {
                return cnt;
            }
        };
    }

    @Test
    void getPost_없으면_404() {
        when(postRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPost(1L, "Jelin"))
                .isInstanceOf(PostException.class)
                .extracting(e -> ((PostException) e).getBaseErrorCode())
                .isEqualTo(PostErrorCode.POST_NOT_FOUND);
    }

    @Test
    void getPost_성공하면_댓글수와_좋아요여부_포함() {
        Post post = postOf(1L, "Jelin", Instant.now());
        when(postRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(post));
        when(commentRepository.countByPostIdAndDeletedAtIsNull(1L)).thenReturn(5L);
        when(postLikeRepository.existsById(new PostLikeId(1L, "Jelin"))).thenReturn(true);

        PostResponseDTO.PostDTO result = service.getPost(1L, "Jelin");

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.commentCount()).isEqualTo(5L);
        assertThat(result.isLiked()).isTrue();
    }

    @Test
    void getPosts_커서가_없으면_첫페이지_조회() {
        Post post = postOf(1L, "Jelin", Instant.parse("2026-01-01T00:00:00Z"));
        when(postRepository.findFirstPage(any(Pageable.class))).thenReturn(List.of(post));
        when(postRepository.countByDeletedAtIsNull()).thenReturn(1L);
        when(commentRepository.countByPostIds(anyList())).thenReturn(List.of());
        when(postLikeRepository.findLikedPostIds(anyString(), anyList())).thenReturn(List.of());

        PageResponse<PostResponseDTO.PostDTO> result = service.getPosts(null, 10, "Jelin");

        assertThat(result.items()).hasSize(1);
        assertThat(result.total()).isEqualTo(1L);
        assertThat(result.nextCursor()).isNull();
        verify(postRepository, never()).findNextPage(any(), any(), any());
    }

    @Test
    void getPosts_커서가_있으면_디코딩해서_다음페이지_조회() {
        Instant cursorCreatedAt = Instant.parse("2026-01-01T00:00:00Z");
        String cursor = new Cursor(cursorCreatedAt, 5L).encode();
        when(postRepository.findNextPage(eq(cursorCreatedAt), eq(5L), any(Pageable.class)))
                .thenReturn(List.of());
        when(postRepository.countByDeletedAtIsNull()).thenReturn(0L);

        service.getPosts(cursor, 10, "Jelin");

        verify(postRepository).findNextPage(eq(cursorCreatedAt), eq(5L), any(Pageable.class));
        verify(postRepository, never()).findFirstPage(any());
    }

    @Test
    void getPosts_size보다_한개_더_오면_다음페이지가_있고_마지막_1개는_잘라낸다() {
        Post p1 = postOf(1L, "a", Instant.parse("2026-01-03T00:00:00Z"));
        Post p2 = postOf(2L, "b", Instant.parse("2026-01-02T00:00:00Z"));
        Post extra = postOf(3L, "c", Instant.parse("2026-01-01T00:00:00Z"));
        when(postRepository.findFirstPage(any(Pageable.class))).thenReturn(List.of(p1, p2, extra));
        when(postRepository.countByDeletedAtIsNull()).thenReturn(3L);
        when(commentRepository.countByPostIds(anyList())).thenReturn(List.of());
        when(postLikeRepository.findLikedPostIds(eq("Jelin"), anyList())).thenReturn(List.of());

        PageResponse<PostResponseDTO.PostDTO> result = service.getPosts(null, 2, "Jelin");

        assertThat(result.items()).extracting(PostResponseDTO.PostDTO::id).containsExactly(1L, 2L);
        assertThat(result.nextCursor()).isEqualTo(new Cursor(p2.getCreatedAt(), 2L).encode());
    }

    @Test
    void getPosts_댓글수와_좋아요를_postId별로_매핑() {
        Post p1 = postOf(1L, "a", Instant.now());
        Post p2 = postOf(2L, "b", Instant.now());
        when(postRepository.findFirstPage(any(Pageable.class))).thenReturn(List.of(p1, p2));
        when(postRepository.countByDeletedAtIsNull()).thenReturn(2L);
        when(commentRepository.countByPostIds(List.of(1L, 2L)))
                .thenReturn(List.of(countOf(1L, 4L)));
        when(postLikeRepository.findLikedPostIds("Jelin", List.of(1L, 2L)))
                .thenReturn(List.of(2L));

        PageResponse<PostResponseDTO.PostDTO> result = service.getPosts(null, 10, "Jelin");

        PostResponseDTO.PostDTO dto1 = result.items().get(0);
        PostResponseDTO.PostDTO dto2 = result.items().get(1);
        assertThat(dto1.commentCount()).isEqualTo(4L);
        assertThat(dto1.isLiked()).isFalse();
        assertThat(dto2.commentCount()).isZero();
        assertThat(dto2.isLiked()).isTrue();
    }

    @Test
    void getPosts_조회결과가_없으면_댓글수_좋아요_조회를_하지_않는다() {
        when(postRepository.findFirstPage(any(Pageable.class))).thenReturn(List.of());
        when(postRepository.countByDeletedAtIsNull()).thenReturn(0L);

        PageResponse<PostResponseDTO.PostDTO> result = service.getPosts(null, 10, "Jelin");

        assertThat(result.items()).isEmpty();
        assertThat(result.nextCursor()).isNull();
        verify(commentRepository, never()).countByPostIds(anyList());
        verify(postLikeRepository, never()).findLikedPostIds(anyString(), anyList());
    }
}
