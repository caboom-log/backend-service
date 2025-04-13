package site.caboomlog.backendservice.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostPageResponse<T> {
    private List<T> posts;
    private long totalElements;
    private int totalPages;
    private int currentPage;
}