package com.apr.community.domain.comment.service;

import com.apr.community.domain.comment.converter.CommentConverter;
import com.apr.community.domain.comment.dto.CommentResponseDTO;
import com.apr.community.domain.comment.entity.Comment;
import com.apr.community.domain.comment.repository.CommentRepository;
import com.apr.community.domain.post.exception.PostErrorCode;
import com.apr.community.domain.post.exception.PostException;
import com.apr.community.domain.post.repository.PostRepository;
import com.apr.community.global.pagination.Cursor;
import com.apr.community.global.pagination.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    // 댓글 목록: 최신순(created_at DESC, id DESC) 커서 페이지네이션
    public PageResponse<CommentResponseDTO.CommentDTO> getComments(Long postId, String cursor, int size) {
        // 글이 없거나 삭제됐으면 404
        if (!postRepository.existsByIdAndDeletedAtIsNull(postId)) {
            throw new PostException(PostErrorCode.POST_NOT_FOUND);
        }

        // size+1개 조회해서 다음 페이지 여부 판단
        Pageable limit = PageRequest.of(0, size + 1);
        List<Comment> rows;
        if (cursor == null || cursor.isBlank()) {
            rows = commentRepository.findFirstPage(postId, limit);
        } else {
            Cursor c = Cursor.decode(cursor); // 잘못된 커서 → 400 PAGE400_1
            rows = commentRepository.findNextPage(postId, c.createdAt(), c.id(), limit);
        }

        boolean hasNext = rows.size() > size;
        List<Comment> comments = hasNext ? rows.subList(0, size) : rows;

        List<CommentResponseDTO.CommentDTO> items = comments.stream()
                .map(CommentConverter::toCommentDTO)
                .toList();

        // 다음 페이지가 있으면 이번 페이지 마지막 항목으로 커서 생성
        String nextCursor = null;
        if (hasNext) {
            Comment last = comments.get(comments.size() - 1);
            nextCursor = new Cursor(last.getCreatedAt(), last.getId()).encode();
        }

        long total = commentRepository.countByPostIdAndDeletedAtIsNull(postId);
        return new PageResponse<>(items, total, nextCursor);
    }
}
