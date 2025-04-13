package site.caboomlog.backendservice.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import site.caboomlog.backendservice.blog.dto.TeamBlogMemberResponse;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
public class PostDetailResponse {
    private Long postId;
    private TeamBlogMemberResponse writer;
    private TeamBlogMemberResponse modifier;
    private String title;
    private String content;
    private boolean postPublic;
    private Long viewCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    private List<String> categoryNames;
}
