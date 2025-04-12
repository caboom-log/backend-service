package site.caboomlog.backendservice.category.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateCategoryRequest {
    private Long categoryPid;
    private String categoryName;
    private int topicId;
    private boolean categoryPublic;
}
