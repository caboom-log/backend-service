package site.caboomlog.backendservice.post.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.caboomlog.backendservice.common.annotation.LoginMember;
import site.caboomlog.backendservice.common.dto.ApiResponse;
import site.caboomlog.backendservice.post.dto.CreatePostRequest;
import site.caboomlog.backendservice.post.dto.PostDetailResponse;
import site.caboomlog.backendservice.post.service.BlogPostService;

@Slf4j
@RestController
@RequestMapping("/api/blogs/{blogFid}/posts")
@RequiredArgsConstructor
public class BlogPostController {
    private final BlogPostService blogPostService;

    /**
     * 게시글을 생성합니다.
     *
     * @param blogFid 블로그 식별자
     * @param mbNo 로그인된 사용자 식별자 (작성자)
     * @param request 게시글 생성 요청 본문
     * @return 201 Created 응답
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> writePost(@PathVariable("blogFid") String blogFid,
                                                 @LoginMember Long mbNo,
                                                 @RequestBody CreatePostRequest request) {
        blogPostService.createPost(blogFid, mbNo, request);
        log.info("post request: {}", request);

        return ResponseEntity.status(201)
                .body(ApiResponse.created());
    }

    /**
     * 게시글 단일 조회
     * <p>비공개 게시글은 작성자 본인만 조회 가능합니다.</p>
     *
     * @param blogFid 블로그 식별자
     * @param postId 게시글 식별자
     * @param mbNo (선택) 로그인된 사용자 식별자
     * @return 게시글 상세 응답
     */
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(@PathVariable("blogFid") String blogFid,
                                                                   @PathVariable("postId") Long postId,
                                                                   @LoginMember(required = false) Long mbNo) {
        PostDetailResponse response = blogPostService.getPostDetail(blogFid, postId, mbNo);
        return ResponseEntity.ok()
                .body(ApiResponse.ok(response));
    }

}
