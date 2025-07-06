package site.caboomlog.backendservice.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.caboomlog.backendservice.comment.dto.CommentPageResponse;
import site.caboomlog.backendservice.comment.dto.CommentResponse;
import site.caboomlog.backendservice.comment.service.CommentService;
import site.caboomlog.backendservice.common.annotation.LoginMember;
import site.caboomlog.backendservice.common.dto.ApiResponse;

@RestController
@RequestMapping("/api/blogs/{blogFid}/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

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
}
