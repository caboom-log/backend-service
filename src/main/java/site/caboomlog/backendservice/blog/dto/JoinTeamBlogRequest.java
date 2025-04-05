package site.caboomlog.backendservice.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class JoinTeamBlogRequest {
    @NotBlank(message = "blogMbNickname은 필수 입력값입니다.")
    @Size(max = 50, message = "최대 50자까지 입력할 수 있습니다.")
    private String blogMbNickname;
}
