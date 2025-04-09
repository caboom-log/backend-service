package site.caboomlog.backendservice.blog.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import site.caboomlog.backendservice.blog.advice.BlogControllerAdvice;
import site.caboomlog.backendservice.blog.advice.TeamBlogOwnerControllerAdvice;
import site.caboomlog.backendservice.blog.exception.AlreadyInvitedException;
import site.caboomlog.backendservice.blog.service.TeamBlogOwnerService;
import site.caboomlog.backendservice.common.annotation.LoginMemberArgumentResolver;
import site.caboomlog.backendservice.common.advice.CommonControllerAdvice;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.common.exception.UnauthenticatedException;
import site.caboomlog.backendservice.common.interceptor.AuthHeaderInterceptor;
import site.caboomlog.backendservice.member.entity.Member;
import site.caboomlog.backendservice.member.exception.MemberNotFoundException;
import site.caboomlog.backendservice.member.exception.MemberWithdrawException;
import site.caboomlog.backendservice.member.repository.MemberRepository;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TeamBlogOwnerController.class)
@Import(TeamBlogOwnerControllerTest.MockConfig.class)
class TeamBlogOwnerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    TeamBlogOwnerService teamBlogOwnerService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    LoginMemberArgumentResolver loginMemberArgumentResolver;

    Optional<Member> testMember = Optional.of(Member.ofExistingMember(1L, "teest@test.com",
            "세연", "1234qwer",
            "010-0000-0000", null, null));

    @TestConfiguration
    static class MockConfig {
        @Bean
        public TeamBlogOwnerService teamBlogOwnerService() {
            return Mockito.mock(TeamBlogOwnerService.class);
        }

        @Bean
        public MemberRepository memberRepository() {
            return Mockito.mock(MemberRepository.class);
        }

        @Bean
        public AuthHeaderInterceptor authHeaderInterceptor() {
            return new AuthHeaderInterceptor();
        }
    }

    @BeforeEach
    void setup(@Autowired HandlerMethodArgumentResolver loginMemberArgumentResolver,
               @Autowired AuthHeaderInterceptor authHeaderInterceptor) {

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TeamBlogOwnerController(teamBlogOwnerService))
                .setCustomArgumentResolvers(loginMemberArgumentResolver)
                .addInterceptors(authHeaderInterceptor)
                .setControllerAdvice(TeamBlogOwnerControllerAdvice.class,
                        BlogControllerAdvice.class,
                        CommonControllerAdvice.class)
                .build();
    }

    @Test
    @DisplayName("멤버 초대 실패 - 잘못된 요청")
    void inviteMemberFail_BadRequest() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.when(memberRepository.findByMbEmail(anyString())).thenReturn(testMember);

        // when & then
        mockMvc.perform(post("/api/blogs/caboom/members")
                .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                  {"mbEmail" :  ""}
                 """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andDo(print());
    }

    @Test
    @DisplayName("멤버 초대 실패 - 블로그 소유자 권한이 없음")
    void inviteMemberFail_Unauthenticated() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.when(memberRepository.findByMbEmail(anyString())).thenReturn(testMember);
        Mockito.doThrow(new UnauthenticatedException("블로그 소유자가 아님"))
                .when(teamBlogOwnerService).inviteMember(anyLong(), anyString(), anyString());

        // when & then
        mockMvc.perform(post("/api/blogs/caboom/members")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                          {"mbEmail" : "%s" }
                         """, "test@test.com")))
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.status").value("ERROR"))
                        .andDo(print());
    }

    @Test
    @DisplayName("멤버 초대 실패 - 팀 블로그가 아님")
    void inviteMemberFail_NotTeamBlog() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.when(memberRepository.findByMbEmail(anyString())).thenReturn(testMember);
        Mockito.doThrow(new BadRequestException("멤버 초대는 팀 블로그만 가능합니다."))
                .when(teamBlogOwnerService).inviteMember(anyLong(), anyString(), anyString());

        // when & then
        mockMvc.perform(post("/api/blogs/caboom/members")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                          {"mbEmail" : "%s" }
                         """, "test@test.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andDo(print());
    }

    @Test
    @DisplayName("멤버 초대 실패 - 존재하지 않는 멤버")
    void inviteMemberFail_MemberNotFound() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.when(memberRepository.findByMbEmail(anyString())).thenReturn(testMember);
        Mockito.doThrow(new MemberNotFoundException("존재하지 않는 회원입니다."))
                .when(teamBlogOwnerService).inviteMember(anyLong(), anyString(), anyString());

        // when & then
        mockMvc.perform(post("/api/blogs/caboom/members")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                          {"mbEmail" : "%s" }
                         """, "test@test.com")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andDo(print());
    }

    @Test
    @DisplayName("멤버 초대 실패 - 탈퇴한 멤버")
    void inviteMemberFail_WithdrawalMember() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.doThrow(new MemberWithdrawException("이미 탈퇴한 회원입니다."))
                .when(teamBlogOwnerService).inviteMember(anyLong(), anyString(), anyString());

        // when & then
        mockMvc.perform(post("/api/blogs/caboom/members")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                          {"mbEmail" : "%s" }
                         """, "test@test.com")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andDo(print());
    }

    @Test
    @DisplayName("멤버 초대 성공 - 최초 초대 or 초대 이력이 있으나 상대방이 거절한 경우")
    void inviteMemberSuccess() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.when(memberRepository.findByMbEmail(anyString())).thenReturn(testMember);
        Mockito.doNothing()
                .when(teamBlogOwnerService).inviteMember(anyLong(), anyString(), anyString());

        // when & then
        mockMvc.perform(post("/api/blogs/caboom/members")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                          {"mbEmail" : "%s" }
                         """, "test@test.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andDo(print());
    }

    @Test
    @DisplayName("멤버 초대 실패 - 이미 초대됨")
    void inviteMemberFail_AlreadyInvited() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.when(memberRepository.findByMbEmail(anyString())).thenReturn(testMember);
        Mockito.doThrow(new AlreadyInvitedException("이미 초대된 회원입니다."))
                .when(teamBlogOwnerService).inviteMember(anyLong(), anyString(), anyString());

        // when & then
        mockMvc.perform(post("/api/blogs/caboom/members")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                          {"mbEmail" : "%s" }
                         """, "test@test.com")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andDo(print());
    }

    @Test
    @DisplayName("특정 멤버 추방 실패 - 소유자가 아님")
    void kickSingleMemberFail_Unauthenticated() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.doThrow(new UnauthenticatedException("블로그 소유자가 아닙니다"))
                .when(teamBlogOwnerService).kickMember(anyString(), anyLong(), anyString());

        // when & then
        mockMvc.perform(delete("/api/blogs/caboom/members/test-nickname")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andDo(print());
    }

    @Test
    @DisplayName("특정 멤버 추방 실패 - 존재하지 않는 회원")
    void kickSingleMemberFail_MemberNotFound() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.doThrow(new MemberNotFoundException("멤버가 존재하지 않음"))
                .when(teamBlogOwnerService).kickMember(anyString(), anyLong(), anyString());

        // when & then
        mockMvc.perform(delete("/api/blogs/caboom/members/test-nickname")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andDo(print());
    }

    @Test
    @DisplayName("특정 멤버 추방 성공")
    void kickSingleMemberSuccess() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.doNothing()
                .when(teamBlogOwnerService).kickMember(anyString(), anyLong(), anyString());

        // when & then
        mockMvc.perform(delete("/api/blogs/caboom/members/test-nickname")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString()))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andDo(print());
    }

    @Test
    @DisplayName("모든 멤버 추방 성공")
    void kickAllMembersSuccess() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.doNothing()
                .when(teamBlogOwnerService).kickAllMembers(anyString(), anyLong());

        // when & then
        mockMvc.perform(delete("/api/blogs/caboom/members")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString()))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andDo(print());
    }

    @Test
    @DisplayName("모든 멤버 추방 실패 - 소유자가 아님")
    void kickAllMembersFail_Unauthenticated() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.doThrow(new UnauthenticatedException("소유자가 아닙니다."))
                .when(teamBlogOwnerService).kickAllMembers(anyString(), anyLong());

        // when & then
        mockMvc.perform(delete("/api/blogs/caboom/members")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andDo(print());
    }

    @Test
    @DisplayName("블로그 소유자 권한 위임 실패 - 소유자가 아님")
    void transferOwnershipFail_Unauthenticated() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.doThrow(new UnauthenticatedException("소유자가 아닙니다."))
                .when(teamBlogOwnerService).transferOwnership(anyString(), anyLong(), anyString());
        String randomUuid = UUID.randomUUID().toString();

        // when & then
        mockMvc.perform(put("/api/blogs/caboom/transfer-ownership")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"mbUuid\" : \"%s\"}", randomUuid)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("블로그 소유자 권한 위임 실패 - 팀블로그가 아님")
    void transferOwnershipFail_NotTeamBlog() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.doThrow(new BadRequestException("팀 블로그가 아님"))
                .when(teamBlogOwnerService).transferOwnership(anyString(), anyLong(), anyString());
        String randomUuid = UUID.randomUUID().toString();

        // when & then
        mockMvc.perform(put("/api/blogs/caboom/transfer-ownership")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"mbUuid\" : \"%s\"}", randomUuid)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("블로그 소유자 권한 위임 성공")
    void transferOwnershipSuccess() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.doNothing()
                .when(teamBlogOwnerService).transferOwnership(anyString(), anyLong(), anyString());
        String randomUuid = UUID.randomUUID().toString();

        // when & then
        mockMvc.perform(put("/api/blogs/caboom/transfer-ownership")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"mbUuid\" : \"%s\"}", randomUuid)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

}