package site.caboomlog.backendservice.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@Setter
@AllArgsConstructor
@Getter
public class CreateBlogRequest {

    @NotBlank(message = "blogFid는 필수 입력값입니다.")
    @Size(min = 3, max = 50, message = "blogFid는 3~50글자여야 합니다.")
    private String blogFid;

    @NotBlank(message = "mbNickname은 필수 입력값입니다.")
    @Size(max = 50, message = "mbNickname은 최대 50자까지 입력할 수 있습니다.")
    private String mbNickname;

    @NotBlank(message = "blogName은 필수 입력값입니다.")
    @Size(max = 50, message = "blogName은 최대 50자까지 입력할 수 있습니다.")
    private String blogName;

    @Size(max = 100, message = "blogDesc는 최대 100자까지 입력할 수 있습니다.")
    private String blogDesc;

    @NotNull(message = "blogIsPublic은 필수 입력값입니다.")
    private boolean blogPublic;

    @NotBlank(message = "blogType은 필수입니다.")
    @Pattern(regexp = "personal|team", message = "blogType은 'personal' 또는 'team'만 입력 가능합니다.")
    private String blogType;
}
