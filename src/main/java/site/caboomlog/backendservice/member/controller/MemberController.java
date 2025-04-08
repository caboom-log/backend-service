package site.caboomlog.backendservice.member.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import site.caboomlog.backendservice.common.annotation.LoginMember;
import site.caboomlog.backendservice.common.dto.ApiResponse;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.member.dto.GetMemberResponse;
import site.caboomlog.backendservice.member.service.MemberService;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 현재 로그인한 사용자의 회원 정보를 조회합니다.
     *
     * <p>요청 헤더에서 인증된 회원 번호(mbNo)를 추출하여 해당 회원의 정보를 조회합니다.
     * 회원 번호가 유효하지 않으면 {@link BadRequestException}이 발생합니다.</p>
     *
     * <p>정상적으로 조회되면 {@link GetMemberResponse} 형태로 회원 정보를 반환합니다.</p>
     *
     * @param mbNo 로그인된 사용자의 회원 번호 (헤더에서 주입됨)
     * @return {@link ApiResponse}에 담긴 회원 정보
     * @throws BadRequestException mbNo가 null이거나 0 이하인 경우
     */
    @GetMapping("/api/members")
    public ResponseEntity<ApiResponse<GetMemberResponse>> getMember(@LoginMember Long mbNo) {
        if (mbNo == null || mbNo <= 0) {
            throw new BadRequestException("Invalid MbNo");
        }
        GetMemberResponse response = memberService.getMemberByMbNo(mbNo);
        return ResponseEntity.ok()
                .body(ApiResponse.ok(response));
    }

}
