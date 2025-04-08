package site.caboomlog.backendservice.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TeamBlogMemberResponse {
    String mbUuid;
    String mbNickname;
    String mainBlogFid;
}
