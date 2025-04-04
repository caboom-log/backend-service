package site.caboomlog.backendservice.blog.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class InviteMemberRequest {
    private Long mbNo;
}
