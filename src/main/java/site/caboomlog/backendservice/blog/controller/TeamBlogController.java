package site.caboomlog.backendservice.blog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.caboomlog.backendservice.blog.dto.JoinTeamBlogRequest;
import site.caboomlog.backendservice.blog.dto.TeamBlogMemberPageResponse;
import site.caboomlog.backendservice.blog.dto.TeamBlogMemberResponse;
import site.caboomlog.backendservice.blog.service.TeamBlogService;
import site.caboomlog.backendservice.common.annotation.LoginMember;
import site.caboomlog.backendservice.common.dto.ApiResponse;
import site.caboomlog.backendservice.common.exception.BadRequestException;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class TeamBlogController {

    private final TeamBlogService teamBlogService;

    /**
     * 팀 블로그 초대를 수락하고 블로그에 가입합니다.
     *
     * <p>해당 API는 사용자가 수신한 팀 블로그 초대를 수락하고 블로그에 가입할 수 있도록 합니다.</p>
     * <p>요청 본문에는 블로그 내에서 사용할 닉네임이 포함되어야 하며, 닉네임 중복 여부를 포함한 다양한 유효성 검사가 수행됩니다.</p>
     *
     * <ul>
     *   <li>블로그가 존재하고 팀 블로그여야 합니다.</li>
     *   <li>해당 블로그에 대한 초대가 존재하고 상태가 <b>PENDING</b>이어야 합니다.</li>
     *   <li>닉네임 중복이 없어야 하며, 이미 가입된 경우가 아니어야 합니다.</li>
     * </ul>
     *
     * @param blogFid 블로그 식별자 (URL 경로에 포함)
     * @param mbNo 로그인된 회원의 고유 번호
     * @param request 블로그 닉네임을 포함한 요청 본문
     * @return 가입 성공 시 200 OK 응답
     *
     * @throws BlogNotFoundException 블로그가 존재하지 않는 경우
     * @throws BadRequestException 초대 상태가 유효하지 않거나 이미 가입된 경우
     * @throws BlogMemberNicknameConflictException 닉네임이 중복된 경우
     */
    @PostMapping("/{blogFid}/join")
    public ResponseEntity<ApiResponse<String>> joinTeamBlog(@PathVariable("blogFid") String blogFid,
                                                    @LoginMember Long mbNo,
                                                    @RequestBody @Valid JoinTeamBlogRequest request) {
        String blogMbNickname = request.getBlogMbNickname();
        teamBlogService.joinTeamBlog(blogFid, mbNo, blogMbNickname);
        return ResponseEntity.ok()
                .body(ApiResponse.ok(null));
    }

    /**
     * 팀 블로그의 일반 멤버 목록을 조회합니다.
     *
     * <p>해당 블로그의 <b>ROLE_MEMBER</b> 권한을 가진 사용자 목록을 페이징 형태로 반환합니다.</p>
     * <p>인증되지 않은 사용자도 접근 가능한 공개 API입니다.</p>
     *
     * @param blogFid 블로그 식별자 (URL 경로에 포함)
     * @param pageable 페이지 번호, 크기, 정렬 기준 등의 페이징 정보 (기본 정렬은 mbNickname ASC)
     * @return 멤버 목록을 담은 {@link TeamBlogMemberPageResponse}를 포함하는 200 OK 응답
     */
    @GetMapping("/{blogFid}/members")
    public ResponseEntity<ApiResponse<TeamBlogMemberPageResponse>> getMembers(
            @PathVariable("blogFid") String blogFid,
            @PageableDefault(size = 10, page = 0, sort = "mbNickname,asc") Pageable pageable) {
        Page<TeamBlogMemberResponse> result = teamBlogService.getMembers(blogFid, pageable);
        return ResponseEntity.ok()
                .body(ApiResponse.ok(new TeamBlogMemberPageResponse(
                        result.getContent(),
                        result.getTotalElements(),
                        result.getTotalPages(),
                        result.getNumber()
                )));
    }
}
