package site.caboomlog.backendservice.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import site.caboomlog.backendservice.comment.dto.CommentPageResponse;
import site.caboomlog.backendservice.comment.dto.CommentResponse;
import site.caboomlog.backendservice.comment.dto.WriteCommentRequest;
import site.caboomlog.backendservice.comment.service.CommentService;
import site.caboomlog.backendservice.common.annotation.LoginMember;
import site.caboomlog.backendservice.common.dto.ApiResponse;

@RestController
@RequestMapping("/api/blogs/{blogFid}/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 특정 게시글의 댓글 목록을 조회합니다.
     * <p>비공개 댓글은 블로그 멤버 여부에 따라 마스킹 처리됩니다.</p>
     *
     * @param blogFid 블로그 식별자
     * @param postId 게시글 식별자
     * @param pageable 페이징 정보 (기본: 10개씩, 등록일 기준 오름차순 정렬)
     * @param mbNo 로그인 사용자 번호 (비로그인 시 null)
     * @return 댓글 목록 및 페이징 메타 정보 응답
     */
    @GetMapping
    public ApiResponse<CommentPageResponse> getAllComments(
            @PathVariable("blogFid") String blogFid,
            @PathVariable("postId") Long postId,
            @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable,
            @LoginMember(required = false) Long mbNo) {
        Page<CommentResponse> comments = commentService.getAllComments(mbNo, postId, blogFid, pageable);
        return ApiResponse.ok(new CommentPageResponse(comments.getContent(), comments.getTotalElements(),
                comments.getTotalPages(), pageable.getPageNumber()));
    }

    /**
     * 특정 게시글에 댓글을 작성합니다.
     * <p>비공개 게시글의 경우 블로그 멤버만 댓글을 작성할 수 있으며, 공개 게시글은 유효 권한이 필요합니다.</p>
     *
     * @param blogFid 블로그 식별자
     * @param postId 게시글 식별자
     * @param mbNo 로그인 사용자 번호
     * @param request 댓글 작성 요청 DTO
     * @return 성공 여부만 포함된 API 응답
     */
    @PostMapping
    public ApiResponse<Void> writeComment(
            @PathVariable("blogFid") String blogFid,
            @PathVariable("postId") Long postId,
            @LoginMember Long mbNo,
            @RequestBody WriteCommentRequest request
    ) {
        commentService.writeComment(blogFid, postId, mbNo, request);
        return ApiResponse.ok(null);
    }
}
