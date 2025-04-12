package site.caboomlog.backendservice.category.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ChangeVisibilityRequest {
    private boolean blogPublic;
}
