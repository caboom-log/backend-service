package site.caboomlog.backendservice.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class WriteCommentRequest {
    private Long parentCommentId;
    private String content;
    private boolean commentPublic;
    private String mbBlogFid;
}
