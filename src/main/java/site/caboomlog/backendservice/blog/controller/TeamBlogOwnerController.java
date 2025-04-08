package site.caboomlog.backendservice.blog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.caboomlog.backendservice.blog.dto.ChangeTeamBlogOwnerRequest;
import site.caboomlog.backendservice.blog.dto.InviteMemberRequest;
import site.caboomlog.backendservice.blog.service.TeamBlogOwnerService;
import site.caboomlog.backendservice.common.annotation.LoginMember;
import site.caboomlog.backendservice.common.dto.ApiResponse;
import site.caboomlog.backendservice.common.exception.BadRequestException;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
@Slf4j
public class TeamBlogOwnerController {

    private final TeamBlogOwnerService teamBlogOwnerService;

    /**
     * 팀 블로그에 멤버를 초대합니다.
     *
     * <p>요청자는 해당 블로그의 소유자여야 하며, 초대 대상자의 {@code mbUuid}가 유효해야 합니다.
     * 초대가 성공하면 초대 정보가 저장되고, 알림 이벤트가 발행되어 초대 대상자에게 알림이 전송됩니다.</p>
     *
     * @param blogFid 블로그의 고유 식별자
     * @param ownerMbNo 현재 요청자의 회원 번호 (헤더에서 주입됨)
     * @param request 초대 대상자의 회원 UUID를 담은 요청 본문
     * @return 초대가 성공하면 201 CREATED 상태 코드와 함께 빈 응답 반환
     * @throws BadRequestException {@code mbUuid}가 null이거나 비어 있는 경우
     */
    @PostMapping("/{blogFid}/members")
    public ResponseEntity<ApiResponse<Void>> inviteMember(@PathVariable("blogFid") String blogFid,
                                                            @LoginMember Long ownerMbNo,
                                                            @RequestBody InviteMemberRequest request) {
        if (request.getMbUuid() == null || request.getMbUuid().isBlank()) {
            log.info(String.format("mbUuid는 null이거나 비어있을 수 없음. mbUuid: %s", request.getMbUuid()));
            throw new BadRequestException("유효하지 않은 mbUuid입니다.");
        }
        teamBlogOwnerService.inviteMember(ownerMbNo, request.getMbUuid(), blogFid);
        return ResponseEntity.status(201)
                .body(ApiResponse.created());
    }

    /**
     * 특정 팀 블로그 멤버를 추방합니다.
     *
     * <p>해당 블로그의 소유자만 요청할 수 있으며, 추방된 멤버에게 알림이 전송됩니다.</p>
     *
     * @param blogFid 블로그의 고유 식별자
     * @param ownerMbNo 블로그 소유자의 회원 번호 (헤더에서 주입됨)
     * @param targetMbNickname 추방할 대상자의 닉네임
     * @return 추방이 성공하면 204 No Content 상태 코드와 함께 빈 응답 반환
     */
    @DeleteMapping("/{blogFid}/members/{targetMbNickname}")
    public ResponseEntity<ApiResponse<Void>> kickMember(@PathVariable("blogFid") String blogFid,
                                                        @LoginMember Long ownerMbNo,
                                                        @PathVariable("targetMbNickname") String targetMbNickname) {
        teamBlogOwnerService.kickMember(blogFid, ownerMbNo, targetMbNickname);
        return ResponseEntity.status(204)
                .body(ApiResponse.success(204, null, null));
    }

    /**
     * 팀 블로그에 소속된 모든 멤버를 일괄 추방합니다.
     *
     * <p>ROLE_MEMBER 권한을 가진 멤버들이 추방 대상이며, 각 멤버는 개별 트랜잭션으로 처리됩니다.</p>
     *
     * @param blogFid 블로그의 고유 식별자
     * @param ownerMbNo 블로그 소유자의 회원 번호 (헤더에서 주입됨)
     * @return 추방이 완료되면 204 No Content 상태 코드와 함께 빈 응답 반환
     */
    @DeleteMapping("/{blogFid}/members")
    public ResponseEntity<ApiResponse<Void>> kickAllMembers(@PathVariable("blogFid") String blogFid,
                                                            @LoginMember Long ownerMbNo) {
        teamBlogOwnerService.kickAllMembers(blogFid, ownerMbNo);
        return ResponseEntity.status(204)
                .body(ApiResponse.success(204, null, null));
    }

    /**
     * 팀 블로그의 소유권을 다른 멤버에게 위임합니다.
     *
     * <p>해당 블로그의 현재 소유자만 요청할 수 있으며, 대상 멤버는 해당 블로그의
     * ROLE_MEMBER 또는 ROLE_COACH 권한을 가진 사용자여야 합니다.</p>
     *
     * @param blogFid 블로그의 고유 식별자
     * @param ownerMbNo 현재 소유자의 회원 번호 (헤더에서 주입됨)
     * @param request 새로운 소유자의 회원 UUID를 담은 요청 본문
     * @return 위임이 완료되면 200 OK 상태 코드와 함께 빈 응답 반환
     * @throws BadRequestException {@code mbUuid}가 null이거나 비어 있는 경우
     */
    @PutMapping("/{blogFid}/transfer-ownership")
    public ResponseEntity<ApiResponse<String>> transferOwnership(@PathVariable("blogFid") String blogFid,
                                                    @LoginMember Long ownerMbNo,
                                                    @RequestBody ChangeTeamBlogOwnerRequest request) {
        String newOwnerMbUuid  = request.getMbUuid();
        if (newOwnerMbUuid == null || newOwnerMbUuid.isBlank()) {
            log.info(String.format("mbUuid는 null이거나 비어있을 수 없음. mbUuid: %s", newOwnerMbUuid));
            throw new BadRequestException("유효하지 않은 mbUuid입니다.");
        }
        teamBlogOwnerService.transferOwnership(blogFid, ownerMbNo, newOwnerMbUuid);
        return ResponseEntity.ok()
                .body(ApiResponse.ok(null));
    }
}
