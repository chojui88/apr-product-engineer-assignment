package com.apr.community.domain.comment.service;

import com.apr.community.domain.comment.dto.CommentResponseDTO;
import com.apr.community.domain.comment.entity.Comment;
import com.apr.community.domain.comment.repository.CommentRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentQueryServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PostRepository postRepository;

    private CommentQueryService service;

    @BeforeEach
    void setUp() {
        service = new CommentQueryService(commentRepository, postRepository);
    }

    private static Comment commentOf(Long id, Instant createdAt) {
        Comment comment = Comment.createComment(10L, "내용", "author");
        ReflectionTestUtils.setField(comment, "id", id);
        ReflectionTestUtils.setField(comment, "createdAt", createdAt);
        return comment;
    }

    @Test
    void getComments_글이_없으면_404() {
        when(postRepository.existsByIdAndDeletedAtIsNull(10L)).thenReturn(false);

        assertThatThrownBy(() -> service.getComments(10L, null, 10))
                .isInstanceOf(PostException.class)
                .extracting(e -> ((PostException) e).getBaseErrorCode())
                .isEqualTo(PostErrorCode.POST_NOT_FOUND);

        verify(commentRepository, never()).findFirstPage(any(), any());
    }

    @Test
    void getComments_size보다_한개_더_오면_nextCursor_있음() {
        Comment c1 = commentOf(3L, Instant.parse("2026-01-03T00:00:00Z"));
        Comment c2 = commentOf(2L, Instant.parse("2026-01-02T00:00:00Z"));
        Comment extra = commentOf(1L, Instant.parse("2026-01-01T00:00:00Z"));
        when(postRepository.existsByIdAndDeletedAtIsNull(10L)).thenReturn(true);
        when(commentRepository.findFirstPage(eq(10L), any(Pageable.class)))
                .thenReturn(List.of(c1, c2, extra));
        when(commentRepository.countByPostIdAndDeletedAtIsNull(10L)).thenReturn(3L);

        PageResponse<CommentResponseDTO.CommentDTO> result = service.getComments(10L, null, 2);

        assertThat(result.items()).extracting(CommentResponseDTO.CommentDTO::id).containsExactly(3L, 2L);
        assertThat(result.nextCursor()).isEqualTo(new Cursor(c2.getCreatedAt(), c2.getId()).encode());
    }

    @Test
    void getComments_정확히_size개면_nextCursor_null() {
        Comment c1 = commentOf(2L, Instant.now());
        Comment c2 = commentOf(1L, Instant.now());
        when(postRepository.existsByIdAndDeletedAtIsNull(10L)).thenReturn(true);
        when(commentRepository.findFirstPage(eq(10L), any(Pageable.class)))
                .thenReturn(List.of(c1, c2));
        when(commentRepository.countByPostIdAndDeletedAtIsNull(10L)).thenReturn(2L);

        PageResponse<CommentResponseDTO.CommentDTO> result = service.getComments(10L, null, 2);

        assertThat(result.items()).hasSize(2);
        assertThat(result.nextCursor()).isNull();
    }

    @Test
    void getComments_커서가_있으면_디코딩해서_findNextPage_호출() {
        Instant cursorCreatedAt = Instant.parse("2026-01-01T00:00:00Z");
        String cursor = new Cursor(cursorCreatedAt, 5L).encode();
        when(postRepository.existsByIdAndDeletedAtIsNull(10L)).thenReturn(true);
        when(commentRepository.findNextPage(eq(10L), eq(cursorCreatedAt), eq(5L), any(Pageable.class)))
                .thenReturn(List.of());
        when(commentRepository.countByPostIdAndDeletedAtIsNull(10L)).thenReturn(0L);

        service.getComments(10L, cursor, 10);

        verify(commentRepository).findNextPage(eq(10L), eq(cursorCreatedAt), eq(5L), any(Pageable.class));
        verify(commentRepository, never()).findFirstPage(any(), any());
    }
}
