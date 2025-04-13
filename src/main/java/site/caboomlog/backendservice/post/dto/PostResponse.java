package site.caboomlog.backendservice.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import site.caboomlog.backendservice.blog.dto.TeamBlogMemberResponse;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
public class PostResponse {
    private Long postId;
    private String blogFid;
    private String title;
    private TeamBlogMemberResponse writer;
    private String summary;
    private String thumbnail;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
    private Long viewCount;
    private List<String> categoryNames;
}
