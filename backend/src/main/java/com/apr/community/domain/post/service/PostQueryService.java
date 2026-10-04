package com.apr.community.domain.post.service;

import com.apr.community.domain.comment.repository.CommentRepository;
import com.apr.community.domain.like.entity.PostLikeId;
import com.apr.community.domain.like.repository.PostLikeRepository;
import com.apr.community.domain.post.converter.PostConverter;
import com.apr.community.domain.post.dto.PostResponseDTO;
import com.apr.community.domain.post.entity.Post;
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {
    //게시글 목록 조회, 댓글 수와 좋아요 여부 응답 DTO로 만들기
    // 게시글마다 x 페이지 단위로 한번에 가져와서 N+1 방지

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;

    public PageResponse<PostResponseDTO.PostDTO> getPosts(String cursor, int size, String requester) {
        // 커서 해석 -> Cursor.decode로 마지막 위치 꺼내기
        // 게시글 정렬 및 순회, size+1 요청, 커서 있으면 커서보다 이전 글 가져오기
        // 다음 페이지 있다면? 마지막 한개 빼고 size개만 반환
        Pageable limit = PageRequest.of(0, size+1);

        List<Post> rows;
        if (cursor == null || cursor.isBlank()) {
            rows = postRepository.findFirstPage(limit);
        }else{
            Cursor c = Cursor.decode(cursor);
            rows = postRepository.findNextPage(c.createdAt(), c.id(), limit);
        }

        boolean hasNext = rows.size() > size;
        List<Post> posts = hasNext ? rows.subList(0, size) : rows;
        List<Long> ids = posts.stream().map(Post::getId).toList();
        // 전체 개수 조회 total (삭제되지 않은 글) 가져오기
        // 현재 페이지 게시글 ID 모으기 ,
        Map<Long, Long> commentCounts = ids.isEmpty() ? Map.of()
                :commentRepository.countByPostIds(ids).stream()
                .collect(Collectors.toMap(CommentRepository.PostCommentCount::getPostId, CommentRepository.PostCommentCount::getCnt));
        // 댓글 수 IN() 으로 조회하고 id별 COUNT 세기, 댓글 없는 게시글은 0으로 처리
        // 좋아요는 현재 요청자 + PostId 함께 조회, 좋아요 여부는 True False
        Set<Long> likeIds = ids.isEmpty() ? Set.of()
                : new HashSet<>(postLikeRepository.findLikedPostIds(requester, ids));
        // likeCount는 Post 필드에 있는 전체 좋아요 수,
        // isLiked는 현재 요청자가 눌렀는지

        List<PostResponseDTO.PostDTO> items = posts.stream()
                .map(p -> PostConverter.toPostDTO(
                        p,
                        commentCounts.getOrDefault(p.getId(), 0L),
                        likeIds.contains(p.getId())))
                .toList();
        // 응답 DTO 만들기
        // 다음 페이지 있다면, nextCursor 만들기, 없으면 nextCursor null 반환

        String nextCursor = null;
        if (hasNext) {
            Post last = posts.get(posts.size() - 1);
            nextCursor = new Cursor(last.getCreatedAt(), last.getId()).encode();
        }

        return new PageResponse<>(items, postRepository.countByDeletedAtIsNull(), nextCursor);
    }

    public PostResponseDTO.PostDTO getPost(Long postId, String requester) {
       // id로 게시글 찾기
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(()-> new PostException(PostErrorCode.POST_NOT_FOUND));

        long commentCount = commentRepository.countByPostIdAndDeletedAtIsNull(postId);
        boolean isLiked = postLikeRepository.existsById(new PostLikeId(postId, requester));

        return PostConverter.toPostDTO(post, commentCount, isLiked);
    }
}
