package site.caboomlog.backendservice.post.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import site.caboomlog.backendservice.blog.entity.Blog;
import site.caboomlog.backendservice.member.entity.Member;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @JoinColumn(name = "blog_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Blog blog;

    @JoinColumn(name = "writer_mb_no")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member writer;

    @JoinColumn(name = "modifier_mb_no")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member modifier;

    @Column(name = "post_title")
    private String postTitle;

    @Column(name = "post_content", columnDefinition = "text")
    private String postContent;

    @Column(name = "post_public", columnDefinition = "tinyint")
    private boolean postPublic;

    @Column(name = "view_count")
    private Long viewCount;

    @Column(name = "thumbnail")
    private String thumbnail;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Post(){}

    private Post(Long postId, Blog blog, Member writer, Member modifier,
                 String postTitle, String postContent, boolean postPublic,
                 Long viewCount, String thumbnail, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.postId = postId;
        this.blog = blog;
        this.writer = writer;
        this.modifier = modifier;
        this.postTitle = postTitle;
        this.postContent = postContent;
        this.postPublic = postPublic;
        this.viewCount = viewCount;
        this.thumbnail = thumbnail;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Post ofNewPost(Blog blog, Member writer,
                                 String postTitle, String postContent, boolean postPublic, String thumbnail) {
        return new Post(null, blog, writer, null,
                postTitle, postContent, postPublic,
                0L, thumbnail, null, null);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
