package site.caboomlog.backendservice.comment.dto;

import lombok.Getter;
import lombok.Setter;
import site.caboomlog.backendservice.comment.entity.Comment;

import java.time.LocalDateTime;

@Getter
public class CommentResponse {
    private Long commentId;
    private Long parentCommentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String content;
    private boolean commentPublic;
    @Setter
    private String mbNickname;
    @Setter
    private String mbProfile;
    @Setter
    private String mbMainBlogFid;

    private CommentResponse(Long commentId, Long parentCommentId, LocalDateTime createdAt, LocalDateTime updatedAt,
                            LocalDateTime deletedAt, String content, boolean commentPublic, String mbNickname,
                            String mbProfile, String mbMainBlogFid) {
        this.commentId = commentId;
        this.parentCommentId = parentCommentId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.content = content;
        this.commentPublic = commentPublic;
        this.mbNickname = mbNickname;
        this.mbProfile = mbProfile;
        this.mbMainBlogFid = mbMainBlogFid;
    }

    public static CommentResponse fromEntity(Comment comment) {
        return new CommentResponse(comment.getCommentId(),
                comment.getParentCommentId(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getDeletedAt(),
                comment.getContent(),
                comment.isCommentPublic(),
                null,
                null,
                null
        );
    }

    public static CommentResponse fromEntityWithMasking(Comment comment) {
        return new CommentResponse(
                comment.getCommentId(),
                comment.getParentCommentId(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getDeletedAt(),
                comment.isCommentPublic() ? comment.getContent() : "비밀 댓글입니다.",
                comment.isCommentPublic(),
                null,
                null,
                null
        );
    }

}