package site.caboomlog.backendservice.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TeamBlogMemberResponse {
    Long mbNo;
    String mbNickname;
    String mainBlogFid;
}
