package site.caboomlog.backendservice.blog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import site.caboomlog.backendservice.blog.dto.BlogInfoResponse;
import site.caboomlog.backendservice.blog.dto.CreateBlogRequest;
import site.caboomlog.backendservice.blog.dto.ModifyBlogInfoRequest;
import site.caboomlog.backendservice.blog.service.BlogService;
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
     * @return 블로그 이름, 설명, 대표 이미지 정보를 담은 {@link BlogInfoResponse}
     *
     * @throws BlogNotFoundException 해당 FID에 해당하는 블로그가 존재하지 않을 경우 예외 발생
     */
    @GetMapping("/{blogFid}")
    public ResponseEntity<BlogInfoResponse> getBlogInfo(@PathVariable("blogFid") String blogFid) {
        BlogInfoResponse blogInfoResponse = blogService.getBlogInfo(blogFid);
        return ResponseEntity.ok(blogInfoResponse);
    }

    /**
     * 블로그를 생성합니다.
     *
     * <p>최대 3개의 블로그만 생성할 수 있으며, 첫 블로그 생성 시 자동으로 메인 블로그로 설정됩니다.</p>
     *
     * @param mbNo 회원 번호 (요청 헤더 caboomlog-mb-no)
     * @param request 블로그 생성 요청 데이터
     * @param bindingResult 요청 검증 결과
     * @return 201 CREATED 응답
     * @throws BadRequestException 요청 데이터가 유효하지 않거나 블로그 수 제한을 초과한 경우
     */
    @PostMapping
    public ResponseEntity<String> createBlog(@RequestHeader("caboomlog-mb-no") Long mbNo,
                                           CreateBlogRequest request,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder builder = new StringBuilder();
            bindingResult.getAllErrors().stream()
                    .forEach(errMsg -> builder.append(errMsg).append("\n"));
            throw new BadRequestException(builder.toString());
        }
        blogService.createBlog(request, mbNo);
        return ResponseEntity.status(201).build();
    }

    /**
     * 블로그 정보를 수정합니다.
     *
     * <p>블로그 이름, 설명, 공개 여부를 수정할 수 있으며, 해당 블로그의 소유자만 변경할 수 있습니다.</p>
     *
     * @param blogFid 블로그 식별자
     * @param mbNo 회원 번호 (요청 헤더 caboomlog-mb-no)
     * @param request 블로그 수정 요청 데이터
     * @return 200 OK 응답
     * @throws BadRequestException 블로그 소유자가 아닌 경우
     */
    @PutMapping("/{blogFid}")
    public ResponseEntity<Void> modifyBlogInfo(@PathVariable("blogFid") String blogFid,
                                               @RequestHeader("caboomlog-mb-no") Long mbNo,
                                               @RequestBody ModifyBlogInfoRequest request) {
        blogService.modifyBlogInfo(blogFid, mbNo, request);
        return ResponseEntity.ok().build();
    }

    /**
     * 메인 블로그를 변경합니다.
     *
     * <p>현재 메인 블로그를 해제하고, 지정한 블로그를 새로운 메인 블로그로 설정합니다. 해당 블로그의 소유자만 변경할 수 있습니다.</p>
     *
     * @param blogFid 새로 메인 블로그로 지정할 블로그의 식별자
     * @param mbNo 회원 번호 (요청 헤더 caboomlog-mb-no)
     * @return 200 OK 응답
     * @throws BadRequestException 기존 메인 블로그가 없거나 권한이 없는 경우
     */
    @PutMapping("/{blogFid}/main")
    public ResponseEntity<Void> switchMainBlogTo(@PathVariable("blogFid") String blogFid,
                                                 @RequestHeader("caboomlog-mb-no") Long mbNo) {
        blogService.switchMainBlogTo(blogFid, mbNo);
        return ResponseEntity.ok().build();
    }

}
