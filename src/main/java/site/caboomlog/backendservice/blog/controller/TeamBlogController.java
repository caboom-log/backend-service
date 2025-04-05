package site.caboomlog.backendservice.blog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import site.caboomlog.backendservice.blog.dto.JoinTeamBlogRequest;
import site.caboomlog.backendservice.blog.service.TeamBlogService;
import site.caboomlog.backendservice.common.exception.BadRequestException;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class TeamBlogController {

    private final TeamBlogService teamBlogService;

    /**
     * 팀 블로그 초대를 수락하고 블로그에 가입합니다.
     *
     * <p>클라이언트는 초대 수락을 위해 블로그 닉네임을 입력해야 하며,
     * 닉네임 중복, 초대 상태 검증 등 유효성 검사 후 가입이 완료됩니다.</p>
     *
     * @param blogFid 블로그 식별자 (URL 상에서 사용되는 blogId)
     * @param mbNo 회원 번호 (요청자 본인, 헤더 caboomlog-mb-no로 전달됨)
     * @param request 블로그 내에서 사용할 닉네임을 담은 요청 본문
     * @param bindingResult 요청 데이터 유효성 검사 결과
     * @return 200 OK 응답
     * @throws BadRequestException 유효하지 않은 닉네임, 초대 상태, 또는 이미 가입된 경우
     */
    @PostMapping("/{blogFid}/join")
    public ResponseEntity<String> joinTeamBlog(@PathVariable("blogFid") String blogFid,
                                               @RequestHeader("caboomlog-mb-no") Long mbNo,
                                               @RequestBody JoinTeamBlogRequest request,
                                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder builder = new StringBuilder();
            bindingResult.getAllErrors().stream()
                    .forEach(err -> builder.append(err.getDefaultMessage()).append("\n"));
            throw new BadRequestException(builder.toString());
        }

        String blogMbNickname = request.getBlogMbNickname();
        teamBlogService.joinTeamBlog(blogFid, mbNo, blogMbNickname);
        return ResponseEntity.ok().build();
    }
}
