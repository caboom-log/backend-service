package site.caboomlog.backendservice.member.controller;

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
import site.caboomlog.backendservice.common.advice.CommonControllerAdvice;
import site.caboomlog.backendservice.common.annotation.LoginMemberArgumentResolver;
import site.caboomlog.backendservice.common.interceptor.AuthHeaderInterceptor;
import site.caboomlog.backendservice.member.advice.MemberControllerAdvice;
import site.caboomlog.backendservice.member.dto.GetMemberResponse;
import site.caboomlog.backendservice.member.entity.Member;
import site.caboomlog.backendservice.member.repository.MemberRepository;
import site.caboomlog.backendservice.member.service.MemberService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MemberController.class)
@Import(MemberControllerTest.MockConfig.class)
class MemberControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LoginMemberArgumentResolver loginMemberArgumentResolver;

    Optional<Member> testMember = Optional.of(
            Member.ofExistingMember(1L, "test@test.com",
                    "caboom", "testpwd",
                    "010-0000-1111", null, null));

    @TestConfiguration
    static class MockConfig {
        @Bean
        MemberService memberService() {
            return Mockito.mock(MemberService.class);
        }

        @Bean
        MemberRepository memberRepository() {
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
                .standaloneSetup(new MemberController(memberService))
                .setCustomArgumentResolvers(loginMemberArgumentResolver)
                .addInterceptors(authHeaderInterceptor)
                .setControllerAdvice(MemberControllerAdvice.class, CommonControllerAdvice.class)
                .build();
    }

    @BeforeEach
    void resetMocks() {
        Mockito.reset(memberService);
    }

    @Test
    @DisplayName("멤버 정보 조회 실패 - 인증 헤더 없음")
    void getMemberFail_NoAuthHeader() throws Exception {
        mockMvc.perform(get("/api/members")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("멤버 정보 조회 성공")
    void getMemberSuccess() throws Exception {
        // given
        GetMemberResponse response = new GetMemberResponse(
                "test-mb-uuid", "test@test.com", "testMain");
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.when(memberService.getMemberByMbNo(anyLong())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/members")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Caboomlog-UID", "test-mb-uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andDo(print())
                .andExpect(jsonPath("$.content.mbUuid").value("test-mb-uuid"));
    }
}