package site.caboomlog.backendservice.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class CommentPageResponse {
    private List<CommentResponse> comments;
    private long totalElements;
    private int totalPages;
    private int currentPage;
}
