package site.caboomlog.backendservice.blog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import site.caboomlog.backendservice.blog.dto.BlogInfoResponse;
import site.caboomlog.backendservice.blog.dto.CreateBlogRequest;
import site.caboomlog.backendservice.blog.service.BlogService;
import site.caboomlog.backendservice.common.exception.BadRequestException;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {

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

}
