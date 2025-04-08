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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import site.caboomlog.backendservice.blog.advice.BlogControllerAdvice;
import site.caboomlog.backendservice.blog.dto.TeamBlogMemberResponse;
import site.caboomlog.backendservice.blog.service.TeamBlogService;
import site.caboomlog.backendservice.common.annotation.LoginMemberArgumentResolver;
import site.caboomlog.backendservice.common.advice.CommonControllerAdvice;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.common.interceptor.AuthHeaderInterceptor;
import site.caboomlog.backendservice.member.entity.Member;
import site.caboomlog.backendservice.member.repository.MemberRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TeamBlogController.class)
@Import(TeamBlogControllerTest.MockConfig.class)
class TeamBlogControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    TeamBlogService teamBlogService;

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
        public TeamBlogService teamBlogService() {
            return Mockito.mock(TeamBlogService.class);
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
                .standaloneSetup(new TeamBlogController(teamBlogService))
                .setCustomArgumentResolvers(loginMemberArgumentResolver,
                        new PageableHandlerMethodArgumentResolver())
                .addInterceptors(authHeaderInterceptor)
                .setControllerAdvice(
                        BlogControllerAdvice.class,
                        CommonControllerAdvice.class)
                .build();
    }

    @Test
    @DisplayName("팀블로그 가입 요청 수락 실패 - 잘못된 요청 바디")
    void joinTeamBlogFail_BadRequestBody() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);

        // when & then
        mockMvc.perform(post("/api/blogs/caboom/join")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                .content("{\"blogMbNickname\" :  \"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("팀블로그 가입 요청 수락 실패 - 잘못된 요청(팀블로그 아니거나, 초대 기록 없거나, 이미 거절했거나)")
    void joinTeamBlogFail_BadRequest() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.doThrow(new BadRequestException("잘못된 요청"))
                .when(teamBlogService).joinTeamBlog(anyString(), anyLong(), anyString());

        // when & then
        mockMvc.perform(post("/api/blogs/caboom/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .content("{\"blogMbNickname\" :  \"test-nickname\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("팀블로그 가입 요청 수락 성공")
    void joinTeamBlogSuccess() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.doNothing()
                .when(teamBlogService).joinTeamBlog(anyString(), anyLong(), anyString());

        // when & then
        mockMvc.perform(post("/api/blogs/caboom/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .content("{\"blogMbNickname\" :  \"test-nickname\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("팀블로그 멤버 조회 성공 - 인증 헤더 없어도 조회 가능")
    void getMembersSuccess() throws Exception {
        // given
        List<TeamBlogMemberResponse> content = List.of(
                new TeamBlogMemberResponse("test-uuid-1", "test-nickname-1", "test-fid-1"),
                new TeamBlogMemberResponse("test-uuid-2", "test-nickname-2", "test-fid-2")
        );
        Page<TeamBlogMemberResponse> members = new PageImpl<>(content, PageRequest.of(0, 10), content.size());
        Mockito.when(teamBlogService.getMembers(anyString(), any(Pageable.class))).thenReturn(members);

        // when & then
        mockMvc.perform(get("/api/blogs/caboom/members")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.content.members").isArray())
                .andExpect(jsonPath("$.content.members[0].mbUuid").value("test-uuid-1"))
                .andExpect(jsonPath("$.content.members[1].mbNickname").value("test-nickname-2"))
                .andExpect(jsonPath("$.content.totalElements").value(2));
    }
}