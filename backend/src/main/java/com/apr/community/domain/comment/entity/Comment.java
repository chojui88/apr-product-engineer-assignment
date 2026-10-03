package com.apr.community.domain.comment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.Instant;

@Entity
@Builder(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@DynamicUpdate
@DynamicInsert
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연관관계 대신 FK 값만 보관 (응답에 postId만 필요, 지연 로딩 이슈 회피)
    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "author", length = 50, nullable = false)
    private String author;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public static Comment createComment(Long postId, String content, String author) {
        return Comment.builder()
                .postId(postId)
                .content(content)
                .author(author)
                .createdAt(Instant.now())
                .build();
    }

    public boolean isOwnedBy(String requester) {
        return author.equals(requester);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }
}
