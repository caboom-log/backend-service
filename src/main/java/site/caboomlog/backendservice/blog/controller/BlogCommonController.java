package site.caboomlog.backendservice.blog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.caboomlog.backendservice.blog.dto.BlogInfoResponse;
import site.caboomlog.backendservice.blog.dto.CreateBlogRequest;
import site.caboomlog.backendservice.blog.dto.ModifyBlogInfoRequest;
import site.caboomlog.backendservice.blog.service.BlogService;
import site.caboomlog.backendservice.common.annotation.LoginMember;
import site.caboomlog.backendservice.common.dto.ApiResponse;
import site.caboomlog.backendservice.common.exception.BadRequestException;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogCommonController {

    private final BlogService blogService;

    /**
     * 블로그 FID를 기반으로 블로그 정보를 조회합니다.
     *
     * @param blogFid 블로그의 고유 식별자 (FID)
     * @return {@link BlogInfoResponse}를 담은 200 OK 응답
     *
     * @throws BlogNotFoundException 해당 FID에 해당하는 블로그가 존재하지 않을 경우
     */
    @GetMapping("/{blogFid}")
    public ResponseEntity<ApiResponse<BlogInfoResponse>> getBlogInfo(@PathVariable("blogFid") String blogFid) {
        BlogInfoResponse blogInfoResponse = blogService.getBlogInfo(blogFid);
        return ResponseEntity.ok(ApiResponse.ok(blogInfoResponse));
    }

    /**
     * 새로운 블로그를 생성합니다.
     *
     * <p>회원은 최대 3개의 블로그를 생성할 수 있으며, 첫 생성 시 해당 블로그는 자동으로 메인 블로그로 설정됩니다.</p>
     *
     * @param mbNo 로그인한 회원의 고유 번호
     * @param request 생성할 블로그의 정보 (FID, 이름, 설명, 공개 여부 등)
     * @return 생성 성공 시 201 CREATED 응답
     *
     * @throws BadRequestException 요청 데이터가 유효하지 않거나, 블로그 생성 제한(최대 3개)을 초과한 경우
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createBlog(@LoginMember Long mbNo,
                                           @RequestBody @Valid CreateBlogRequest request) {
        blogService.createBlog(request, mbNo);
        return ResponseEntity.status(201)
                .body(ApiResponse.created());
    }

    /**
     * 블로그 정보를 수정합니다.
     *
     * <p>블로그의 이름, 설명, 공개 여부를 변경할 수 있으며, 해당 블로그의 소유자만 수정이 가능합니다.</p>
     *
     * @param blogFid 수정할 블로그의 식별자 (FID)
     * @param mbNo 로그인한 회원의 고유 번호
     * @param request 수정할 블로그 정보
     * @return 수정 성공 시 200 OK 응답
     *
     * @throws BadRequestException 블로그 소유자가 아닌 경우 수정이 불가능합니다.
     */
    @PutMapping("/{blogFid}")
    public ResponseEntity<ApiResponse<Void>> modifyBlogInfo(@PathVariable("blogFid") String blogFid,
                                               @LoginMember Long mbNo,
                                               @RequestBody ModifyBlogInfoRequest request) {
        blogService.modifyBlogInfo(blogFid, mbNo, request);
        return ResponseEntity.ok()
                .body(ApiResponse.ok(null));
    }

    /**
     * 메인 블로그를 변경합니다.
     *
     * <p>현재 메인 블로그를 해제하고, 지정한 블로그를 새로운 메인 블로그로 설정합니다.</p>
     *
     * @param blogFid 메인 블로그로 설정할 블로그의 식별자 (FID)
     * @param mbNo 로그인한 회원의 고유 번호
     * @return 메인 블로그 변경 성공 시 200 OK 응답
     *
     * @throws BadRequestException 기존 메인 블로그가 존재하지 않거나 권한이 없는 경우 발생합니다.
     */
    @PutMapping("/{blogFid}/main")
    public ResponseEntity<ApiResponse<Void>> switchMainBlogTo(@PathVariable("blogFid") String blogFid,
                                                 @LoginMember Long mbNo) {
        blogService.switchMainBlogTo(blogFid, mbNo);
        return ResponseEntity.ok()
                .body(ApiResponse.ok(null));
    }
}
