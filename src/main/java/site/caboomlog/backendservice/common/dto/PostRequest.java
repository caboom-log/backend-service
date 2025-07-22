package site.caboomlog.backendservice.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PostRequest {
    private Long postId;

    private String blogFid;

    @JsonProperty("title")
    private String postTitle;

    private String postContent;

    private String thumbnail;

    private LocalDateTime createdAt;

    private List<String> topics;
}
