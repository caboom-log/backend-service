package site.caboomlog.backendservice.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class PostRequest {
    private Long postId;

    private String blogFid;

    private String postTitle;

    private String postContent;

    private LocalDateTime createdAt;

    private List<String> topics;
}
