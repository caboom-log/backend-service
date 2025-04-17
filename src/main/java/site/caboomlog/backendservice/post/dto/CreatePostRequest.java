package site.caboomlog.backendservice.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CreatePostRequest {
    @NotBlank(message = "제목은 필수 입력값입니다.")
    private String title;
    private String content;
    @Setter
    private List<Long> categoryIds;
    private boolean postPublic;
    private String thumbnail;
}
