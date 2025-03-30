package site.caboomlog.backendservice.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GetMemberResponse {
    private Long mbNo;
    private String mbEmail;
    private String mainBlogFid;
}
