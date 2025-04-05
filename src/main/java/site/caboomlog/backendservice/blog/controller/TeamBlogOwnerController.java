package site.caboomlog.backendservice.blog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.caboomlog.backendservice.blog.dto.InviteMemberRequest;
import site.caboomlog.backendservice.blog.dto.TeamBlogMembersResponse;
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

    /**
     * 팀 블로그의 멤버 목록을 조회합니다.
     *
     * <p>해당 블로그의 소유자만 요청 가능하며, ROLE_MEMBER 권한을 가진 사용자 목록을 반환합니다.</p>
     *
     * @param blogFid 블로그 식별자
     * @param ownerMbNo 블로그 소유자의 회원 번호 (헤더 caboomlog-mb-no)
     * @return 멤버 목록 응답
     */
    @GetMapping("/{blogFid}/members")
    public ResponseEntity<TeamBlogMembersResponse> getMembers(@PathVariable("blogFid") String blogFid,
                                                                      @RequestHeader("caboomlog-mb-no") Long ownerMbNo) {
        TeamBlogMembersResponse response = teamBlogOwnerService.getMembers(blogFid, ownerMbNo);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 팀 블로그 멤버를 추방합니다.
     *
     * <p>블로그 소유자만 요청 가능하며, 추방된 멤버에게 알림이 전송됩니다.</p>
     *
     * @param blogFid 블로그 식별자
     * @param ownerMbNo 블로그 소유자의 회원 번호
     * @param mbNo 추방할 대상자의 회원 번호
     * @return 204 No Content 응답
     */
    @DeleteMapping("/{blogFid}/members/{mbNo}")
    public ResponseEntity<String> kickMember(@PathVariable("blogFid") String blogFid,
                                             @RequestHeader("caboomlog-mb-no") Long ownerMbNo,
                                             @PathVariable("mbNo") Long mbNo) {
        teamBlogOwnerService.kickMember(blogFid, ownerMbNo, mbNo);
        return ResponseEntity.noContent().build();
    }

    /**
     * 해당 블로그에 속한 모든 멤버를 일괄 추방합니다.
     *
     * <p>ROLE_MEMBER 권한을 가진 모든 멤버에 대해 추방 처리를 수행하며, 각 멤버는 개별 트랜잭션으로 처리됩니다.</p>
     *
     * @param blogFid 블로그 식별자
     * @param ownerMbNo 블로그 소유자의 회원 번호
     * @return 204 No Content 응답
     */
    @DeleteMapping("/{blogFid}/members")
    public ResponseEntity<String> kickAllMembers(@PathVariable("blogFid") String blogFid,
                                                 @RequestHeader("caboomlog-mb-no") Long ownerMbNo) {
        teamBlogOwnerService.kickAllMembers(blogFid, ownerMbNo);
        return ResponseEntity.noContent().build();
    }

}
