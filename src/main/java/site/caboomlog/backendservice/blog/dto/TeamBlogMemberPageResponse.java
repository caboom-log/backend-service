package site.caboomlog.backendservice.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TeamBlogMemberPageResponse {
    private List<TeamBlogMemberResponse> members;
    private long totalElements;
    private int totalPages;
    private int currentPage;
}