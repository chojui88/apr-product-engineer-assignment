package com.apr.community.domain.post.entity;

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
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", length = 20, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "author", length = 50, nullable = false)
    private String author;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // 내용(제목·본문) 수정 시에만 갱신. 좋아요·삭제로는 바뀌지 않도록 @LastModifiedDate 대신 update()에서 직접 갱신
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public static Post createPost(String title, String content, String author) {
        Instant now = Instant.now();
        return Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .likeCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public boolean isOwnedBy(String requester) {
        return author.equals(requester);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    public void decreaseLikeCount() {
        if (likeCount > 0) {
            likeCount--;
        }
    }

    public void increaseLikeCount(){
        likeCount++;
    }
}
