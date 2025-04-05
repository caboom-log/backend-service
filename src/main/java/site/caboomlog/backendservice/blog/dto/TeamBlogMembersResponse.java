package site.caboomlog.backendservice.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TeamBlogMembersResponse {
    List<TeamBlogMemberResponse> members = new ArrayList<>();
}
