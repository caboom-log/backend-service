package site.caboomlog.backendservice.blogmember.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.caboomlog.backendservice.blogmember.service.BlogMemberMappingService;
import site.caboomlog.backendservice.common.annotation.LoginMember;
import site.caboomlog.backendservice.common.dto.ApiResponse;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogMemberMappingController {

    private final BlogMemberMappingService blogMemberMappingService;

    /**
     * 현재 사용자가 해당 블로그의 멤버인지 확인합니다.
     *
     * @param blogFid 블로그의 고유 식별자 (FID)
     * @param mbNo 요청한 사용자의 멤버 번호 (caboomlog-mb-no 헤더)
     * @return 사용자가 해당 블로그의 멤버일 경우 {@code isMember: true} 를 포함하는 응답 반환
     *
     * <p>프론트엔드에서는 이 API를 호출하여 '글쓰기' 버튼 등의 UI 노출 여부를 판단할 수 있습니다.</p>
     */
    @GetMapping("/{blogFid}/members/me")
    public ResponseEntity<ApiResponse<Boolean>> checkIfMemberOfBlog(@PathVariable("blogFid") String blogFid,
                                                           @LoginMember Long mbNo) {
        return ResponseEntity.ok(
                ApiResponse.ok(blogMemberMappingService.isMemberOfBlog(mbNo, blogFid)));
    }
}
