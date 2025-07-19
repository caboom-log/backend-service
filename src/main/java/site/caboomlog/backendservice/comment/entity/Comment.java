package site.caboomlog.backendservice.comment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import site.caboomlog.backendservice.blogmember.entity.BlogMemberMapping;
import site.caboomlog.backendservice.post.entity.Post;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
public class Comment {
    @Id
    @Column(name = "comment_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @JoinColumn(name = "post_id")
    @ManyToOne
    private Post post;

    @JoinColumn(name = "blog_member_mapping_id")
    @OneToOne
    private BlogMemberMapping blogMemberMapping;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "content")
    private String content;

    @Column(name = "comment_public", columnDefinition = "tinyint")
    private boolean commentPublic;

    protected Comment(){}

    private Comment(Long commentId, Long parentCommentId, Post post, BlogMemberMapping blogMemberMapping,
                    LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt,
                    String content, boolean commentPublic) {
        this.commentId = commentId;
        this.parentCommentId = parentCommentId;
        this.post = post;
        this.blogMemberMapping = blogMemberMapping;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.content = content;
        this.commentPublic = commentPublic;
    }

    public static Comment ofNewComment(Long parentCommentId, Post post, BlogMemberMapping blogMemberMapping,
                                       String content, boolean commentPublic) {
        return new Comment(null, parentCommentId, post, blogMemberMapping,
                LocalDateTime.now(), null, null,
                content, commentPublic);
    }
}
