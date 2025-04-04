package site.caboomlog.backendservice.blog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.caboomlog.backendservice.blog.dto.InviteMemberRequest;
import site.caboomlog.backendservice.blog.service.TeamBlogOwnerService;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
@Slf4j
public class TeamBlogOwnerController {

    private final TeamBlogOwnerService teamBlogOwnerService;

    /**
     * 팀 블로그 멤버 초대 요청을 처리합니다.
     *
     * <p>요청자는 해당 블로그의 소유자여야 하며, 초대할 회원 번호가 유효해야 합니다.
     * 초대가 성공하면 초대 정보가 저장되고, 알림 이벤트가 발행되어 초대 대상자에게 알림이 전송됩니다.</p>
     *
     * @param blogFid 블로그 식별자
     * @param ownerMbNo 요청자(블로그 소유자)의 회원 번호 (caboomlog-mb-no 헤더에서 전달)
     * @param request 초대 대상자의 회원 번호를 포함하는 요청 본문
     * @return 초대 성공 시 200 OK 응답
     */
    @PostMapping("/{blogFid}/members")
    public ResponseEntity<String> inviteMember(@PathVariable("blogFid") String blogFid,
                                               @RequestHeader("caboomlog-mb-no") Long ownerMbNo,
                                               @RequestBody InviteMemberRequest request) {
        if (request.getMbNo() == null || request.getMbNo() <= 0) {
            log.info(String.format("mbNo는 null이거나 0 이하일 수 없음. mbNo: ", request.getMbNo()));
            ResponseEntity.badRequest().body("유효하지 않은 mbNo입니다.");
        }
        teamBlogOwnerService.inviteMember(ownerMbNo, request.getMbNo(), blogFid);
        return ResponseEntity.ok().build();
    }
}
