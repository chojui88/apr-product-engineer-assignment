package com.apr.community.domain.comment.service;

import com.apr.community.domain.comment.dto.CommentRequestDTO;
import com.apr.community.domain.comment.dto.CommentResponseDTO;
import com.apr.community.domain.comment.entity.Comment;
import com.apr.community.domain.comment.exception.CommentErrorCode;
import com.apr.community.domain.comment.exception.CommentException;
import com.apr.community.domain.comment.repository.CommentRepository;
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
class CommentCommandServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PostRepository postRepository;

    private CommentCommandService service;

    @BeforeEach
    void setUp() {
        service = new CommentCommandService(commentRepository, postRepository);
    }

    private static Comment commentOf(Long id, Long postId, String author) {
        Comment comment = Comment.createComment(postId, "원래 내용", author);
        ReflectionTestUtils.setField(comment, "id", id);
        return comment;
    }

    @Test
    void createComment_글이_없으면_404() {
        when(postRepository.existsByIdAndDeletedAtIsNull(10L)).thenReturn(false);
        CommentRequestDTO.CommentDTO request = new CommentRequestDTO.CommentDTO("내용");

        assertThatThrownBy(() -> service.createComment(10L, request, "Jelin"))
                .isInstanceOf(PostException.class)
                .extracting(e -> ((PostException) e).getBaseErrorCode())
                .isEqualTo(PostErrorCode.POST_NOT_FOUND);

        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_공백만_있으면_400() {
        when(postRepository.existsByIdAndDeletedAtIsNull(10L)).thenReturn(true);
        CommentRequestDTO.CommentDTO request = new CommentRequestDTO.CommentDTO("    ");

        assertThatThrownBy(() -> service.createComment(10L, request, "Jelin"))
                .isInstanceOf(CommentException.class)
                .extracting(e -> ((CommentException) e).getBaseErrorCode())
                .isEqualTo(CommentErrorCode.BLANK_CONTENT);

        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_성공하면_strip해서_저장() {
        when(postRepository.existsByIdAndDeletedAtIsNull(10L)).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));
        CommentRequestDTO.CommentDTO request = new CommentRequestDTO.CommentDTO("  내용  ");

        CommentResponseDTO.CommentDTO result = service.createComment(10L, request, "Jelin");

        assertThat(result.content()).isEqualTo("내용");
        assertThat(result.postId()).isEqualTo(10L);
        assertThat(result.author()).isEqualTo("Jelin");

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo("내용");
    }

    @Test
    void deleteComment_댓글이_없으면_404() {
        when(commentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteComment(1L, "Jelin"))
                .isInstanceOf(CommentException.class)
                .extracting(e -> ((CommentException) e).getBaseErrorCode())
                .isEqualTo(CommentErrorCode.COMMENT_NOT_FOUND);

        verify(postRepository, never()).existsByIdAndDeletedAtIsNull(any());
    }

    @Test
    void deleteComment_부모_글이_삭제됐으면_404() {
        Comment comment = commentOf(1L, 10L, "Jelin");
        when(commentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(comment));
        when(postRepository.existsByIdAndDeletedAtIsNull(10L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteComment(1L, "Jelin"))
                .isInstanceOf(CommentException.class)
                .extracting(e -> ((CommentException) e).getBaseErrorCode())
                .isEqualTo(CommentErrorCode.COMMENT_NOT_FOUND);

        assertThat(comment.isDeleted()).isFalse();
    }

    @Test
    void deleteComment_남의_댓글이면_403_삭제되지_않음() {
        Comment comment = commentOf(1L, 10L, "다른사람");
        when(commentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(comment));
        when(postRepository.existsByIdAndDeletedAtIsNull(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.deleteComment(1L, "Jelin"))
                .isInstanceOf(CommentException.class)
                .extracting(e -> ((CommentException) e).getBaseErrorCode())
                .isEqualTo(CommentErrorCode.COMMENT_FORBIDDEN);

        assertThat(comment.isDeleted()).isFalse();
    }

    @Test
    void deleteComment_본인_댓글이면_성공() {
        Comment comment = commentOf(1L, 10L, "Jelin");
        when(commentRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(comment));
        when(postRepository.existsByIdAndDeletedAtIsNull(10L)).thenReturn(true);

        service.deleteComment(1L, "Jelin");

        assertThat(comment.isDeleted()).isTrue();
    }
}
