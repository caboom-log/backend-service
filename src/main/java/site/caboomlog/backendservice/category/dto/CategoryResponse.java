package site.caboomlog.backendservice.category.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.caboomlog.backendservice.category.entity.Category;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryResponse {
    private Long categoryId;
    private String categoryName;
    private Boolean categoryPublic;
    private Long depth;
    private String topicName;
    private List<CategoryResponse> children = new ArrayList<>();

    public CategoryResponse(Category category) {
        this.categoryId = category.getCategoryId();
        this.categoryName = category.getCategoryName();
        this.categoryPublic = category.getCategoryPublic();
        this.depth = category.getDepth();
        this.topicName = category.getTopic().getTopicName();
    }
}
