package site.caboomlog.backendservice.member.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.member.dto.GetMemberResponse;
import site.caboomlog.backendservice.member.service.MemberService;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 주어진 회원 번호(`mbNo`)를 기반으로 회원 정보를 조회합니다.
     *
     * <p>HTTP 요청 헤더에 포함된 `caboomlog-mb-no` 값을 검증하며,
     * 값이 null이거나 0 이하일 경우 {@link BadRequestException}을 발생시킵니다.</p>
     *
     * @param mbNo HTTP 헤더에서 전달된 회원 번호
     * @return 조회된 회원 정보를 담은 {@link GetMemberResponse}
     * @throws BadRequestException 유효하지 않은 회원 번호가 전달된 경우
     */
    @GetMapping("/api/members")
    public ResponseEntity<GetMemberResponse> getMember(@RequestHeader("caboomlog-mb-no") Long mbNo) {
        if (mbNo == null || mbNo <= 0) {
            throw new BadRequestException("Invalid MbNo");
        }
        GetMemberResponse response = memberService.getMemberByMbNo(mbNo);
        return ResponseEntity.ok(response);
    }

}
