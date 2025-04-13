package site.caboomlog.backendservice.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.caboomlog.backendservice.common.dto.ApiResponse;
import site.caboomlog.backendservice.post.dto.PostPageResponse;
import site.caboomlog.backendservice.post.dto.PostResponse;
import site.caboomlog.backendservice.post.service.PublicPostService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PublicPostController {
    private final PublicPostService publicPostService;

    /**
     * 특정 블로그의 공개 게시글 목록을 조회합니다.
     *
     * @param blogFid 블로그 식별자
     * @param pageable 페이징 정보
     * @return 게시글 목록 및 페이지 메타 정보 응답
     */
    @GetMapping("/blogs/{blogFid}/posts/public")
    public ResponseEntity<ApiResponse<PostPageResponse<PostResponse>>> getAllPublicPosts(
            @PathVariable("blogFid") String blogFid,
            @PageableDefault(size = 5, page = 0, sort = "createdAt,desc") Pageable pageable) {
        Page<PostResponse> posts = publicPostService.getAllPublicPosts(blogFid, pageable);
        return ResponseEntity.ok()
                .body(ApiResponse.ok(new PostPageResponse<>(posts.getContent(),
                        posts.getTotalElements(),
                        posts.getTotalPages(),
                        posts.getNumber())));
    }

    /**
     * 전체 블로그의 공개 게시글 목록을 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 게시글 목록 및 페이지 메타 정보 응답
     */
    @GetMapping("/posts/public")
    public ResponseEntity<ApiResponse<PostPageResponse<PostResponse>>> gettAllPublicPosts(
            @PageableDefault(size = 10, page = 0, sort = "createdAt,desc") Pageable pageable) {
        Page<PostResponse> posts = publicPostService.getAllPublicPosts(pageable);
        return ResponseEntity.ok()
                .body(ApiResponse.ok(new PostPageResponse<>(posts.getContent(),
                        posts.getTotalElements(),
                        posts.getTotalPages(),
                        posts.getNumber())));
    }
}
