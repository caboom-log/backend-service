package site.caboomlog.backendservice.comment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import site.caboomlog.backendservice.member.entity.Member;
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

    @JoinColumn(name = "mb_no")
    @OneToOne
    private Member member;

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
}
