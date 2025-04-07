package site.caboomlog.backendservice.blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
import site.caboomlog.backendservice.blog.dto.BlogInfoResponse;
import site.caboomlog.backendservice.blog.exception.BlogFidDuplicatedException;
import site.caboomlog.backendservice.blog.exception.BlogNotFoundException;
import site.caboomlog.backendservice.blog.exception.InvalidBlogCountRangeException;
import site.caboomlog.backendservice.blog.service.BlogService;
import site.caboomlog.backendservice.common.LoginMemberArgumentResolver;
import site.caboomlog.backendservice.common.advice.CommonControllerAdvice;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.common.interceptor.AuthHeaderInterceptor;
import site.caboomlog.backendservice.member.entity.Member;
import site.caboomlog.backendservice.member.exception.MemberNotFoundException;
import site.caboomlog.backendservice.member.repository.MemberRepository;
import site.caboomlog.backendservice.role.exception.RoleNotFoundException;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BlogCommonController.class)
@Import(BlogCommonControllerTest.MockConfig.class)
class BlogCommonControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    BlogService blogService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    LoginMemberArgumentResolver loginMemberArgumentResolver;

    @Autowired
    ObjectMapper objectMapper;

    Optional<Member> testMember = Optional.of(
            Member.ofExistingMember(1L, "test@test.com",
            "caboom", "testpwd",
            "010-0000-1111", null, null));

    @TestConfiguration
    static class MockConfig {
        @Bean
        public BlogService blogService() {
            return Mockito.mock(BlogService.class);
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
                .standaloneSetup(new BlogCommonController(blogService))
                .setCustomArgumentResolvers(loginMemberArgumentResolver)
                .addInterceptors(authHeaderInterceptor)
                .setControllerAdvice(BlogControllerAdvice.class, CommonControllerAdvice.class)
                .build();
    }

    @BeforeEach
    void resetMocks() {
        Mockito.reset(blogService);
    }


    @Test
    @DisplayName("블로그 정보 조회 성공")
    void getBlogInfo() throws Exception {
        // given
        BlogInfoResponse blogInfoResponse = new BlogInfoResponse(
                "공부 블로그", "공부한 내용을 정리합니다.", null);
        Mockito.when(blogService.getBlogInfo(anyString()))
                        .thenReturn(blogInfoResponse);

        // when & then
        mockMvc.perform(get("/api/blogs/caboom")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.content.blogName").value(blogInfoResponse.getBlogName()))
                .andExpect(jsonPath("$.content.blogDesc").value(blogInfoResponse.getBlogDesc()));
    }

    @Test
    @DisplayName("블로그 정보 조회 실패 - 존재하지 않는 블로그")
    void getBlogInfoFail_BlogNotFound() throws Exception {
        // given
        Mockito.when(blogService.getBlogInfo(anyString()))
                        .thenThrow(new BlogNotFoundException("존재하지 않는 블로그입니다."));

        // when & then
        mockMvc.perform(get("/api/blogs/caboom")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("존재하지 않는 블로그입니다."));
    }

    @Test
    @DisplayName("블로그 생성 실패 - 인증 헤더 없음")
    void createBlogFail_NoAuthHeader() throws Exception {
        // given
        String requestStr =  """
            {
              "blogFid": "caboom",
              "mbNickname": "카붐로그",
              "blogName": "공부 블로그",
              "blogDesc": "안녕하세요",
              "blogPublic": true,
              "blogType": "personal"
            }
            """;

        // when & then
        mockMvc.perform(post("/api/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestStr)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("X-Caboomlog-UID 헤더 없음"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("블로그 생성 실패 - 인증 헤더와 매핑되는 mbNo 없음")
    void createBlogFail_InvalidAuthHeader() throws Exception {
        // given
        String requestStr =  """
            {
              "blogFid": "caboom",
              "mbNickname": "카붐로그",
              "blogName": "공부 블로그",
              "blogDesc": "안녕하세요",
              "blogPublic": true,
              "blogType": "personal"
            }
            """;
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(post("/api/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .content(requestStr)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.code").value(404));
    }

    @ParameterizedTest
    @DisplayName("블로그 생성 실패 - 잘못된 요청")
    @MethodSource("invalidCreateBlogRequestProvider")
    void createBlogFail_BadRequest(String requestStr) throws Exception {
        // given

        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);

        // when & then
        mockMvc.perform(post("/api/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .content(requestStr.getBytes())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.code").value(400));
    }

    private static Stream<String> invalidCreateBlogRequestProvider() {
        return Stream.of(
        """
            {
              "mbNickname": "카붐로그",
              "blogName": "공부 블로그",
              "blogDesc": "필수 필드가 없는 케이스",
              "blogType": "personal"
            }
            """,
            """
            {
              "blogFid": "ca",
              "mbNickname": "세연",
              "blogName": "공부 블로그",
              "blogDesc": "blogFid 길이가 두글자 이하인 케이스",
              "blogPublic": true,
              "blogType": "personal"
            }
            """,
            """
            {
              "blogFid": "ca",
              "mbNickname": "세연",
              "blogName": "공부 블로그",
              "blogDesc": "blogType이 잘못된 케이스",
              "blogPublic": true,
              "blogType": "test"
            }
            """
        );
    }

    @Test
    @DisplayName("블로그 생성 실패 - mbNo 존재하지 않는 경우")
    void createBlogFail_MemberNotFound() throws Exception {
        // given
        String requestStr =  """
            {
              "blogFid": "caboom",
              "mbNickname": "카붐로그",
              "blogName": "공부 블로그",
              "blogDesc": "안녕하세요",
              "blogPublic": true,
              "blogType": "personal"
            }
            """;
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.doThrow(new MemberNotFoundException("존재하지 않는 mbNo 입니다: 1L"))
                .when(blogService).createBlog(any(), anyLong());

        // when & then
        mockMvc.perform(post("/api/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .content(requestStr)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("존재하지 않는 mbNo 입니다: 1L"));
    }

    @Test
    @DisplayName("블로그 생성 실패 - DB Role 테이블에 ROLE_OWNER 없는 경우")
    void createBlogFail_DatabaseException() throws Exception {
        // given
        String requestStr =  """
            {
              "blogFid": "caboom",
              "mbNickname": "카붐로그",
              "blogName": "공부 블로그",
              "blogDesc": "안녕하세요",
              "blogPublic": true,
              "blogType": "personal"
            }
            """;
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.doThrow(new RoleNotFoundException("권한이 존재하지 않습니다: ROLE_OWNER"))
                .when(blogService).createBlog(any(), anyLong());

        // when & then
        mockMvc.perform(post("/api/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .content(requestStr)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("서버에 오류가 발생했습니다."));
    }

    @Test
    @DisplayName("블로그 생성 실패 - 이미 존재하는 blogFid")
    void createBlogFail_DuplicatedBlogFid() throws Exception {
        // given
        String requestStr =  """
            {
              "blogFid": "caboom",
              "mbNickname": "카붐로그",
              "blogName": "공부 블로그",
              "blogDesc": "안녕하세요",
              "blogPublic": true,
              "blogType": "personal"
            }
            """;
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.doThrow(new BlogFidDuplicatedException("이미 사용 중인 blog fid 입니다: caboom"))
                .when(blogService).createBlog(any(), anyLong());

        // when & then
        mockMvc.perform(post("/api/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .content(requestStr)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    @DisplayName("블로그 생성 실패 - 이미 블로그가 3개 존재함")
    void createBlogFail_InvalidBlogCountRange() throws Exception {
        // given
        String requestStr =  """
            {
              "blogFid": "caboom",
              "mbNickname": "카붐로그",
              "blogName": "공부 블로그",
              "blogDesc": "안녕하세요",
              "blogPublic": true,
              "blogType": "personal"
            }
            """;
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.doThrow(new InvalidBlogCountRangeException("블로그는 최대 3개까지 생성 가능합니다."))
                .when(blogService).createBlog(any(), anyLong());

        // when & then
        mockMvc.perform(post("/api/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .content(requestStr)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("블로그는 최대 3개까지 생성 가능합니다."));
    }

    @Test
    @DisplayName("블로그 생성 성공")
    void createBlog() throws Exception {
        // given
        String requestStr =  """
            {
              "blogFid": "caboom",
              "mbNickname": "카붐로그",
              "blogName": "공부 블로그",
              "blogDesc": "안녕하세요",
              "blogPublic": true,
              "blogType": "personal"
            }
            """;
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.doNothing()
                .when(blogService).createBlog(any(), anyLong());

        // when & then
        mockMvc.perform(post("/api/blogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .content(requestStr)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(201));
    }

    @Test
    @DisplayName("블로그 정보 수정 성공 - blogName만 변경")
    void modifyBlogSuccess_modifyBlogName() throws Exception {
        // given
        String requestStr =  """
            {
              "blogName": "changedName"
            }
            """;
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.doNothing()
                .when(blogService).modifyBlogInfo(anyString(), anyLong(), any());

        // when & then
        mockMvc.perform(put("/api/blogs/caboom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .content(requestStr)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("블로그 정보 수정 성공")
    void modifyBlogSuccess() throws Exception {
        // given
        String requestStr =  """
            {
              "blogName": "changedName",
              "blogDesc" : "바꿨습니다.",
              "blogPublic" : true
            }
            """;
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.doNothing()
                .when(blogService).modifyBlogInfo(anyString(), anyLong(), any());

        // when & then
        mockMvc.perform(put("/api/blogs/caboom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .content(requestStr)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("블로그 정보 수정 실패 - 존재하지 않는 블로그")
    void modifyBlogFail_BlogNotFound() throws Exception {
        // given
        String requestStr =  """
            {
              "blogName": "changedName",
              "blogDesc" : "바꿨습니다.",
              "blogPublic" : true
            }
            """;
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.doThrow(new BlogNotFoundException("블로그가 존재하지 않습니다. blogFid: caboom"))
                .when(blogService).modifyBlogInfo(anyString(), anyLong(), any());

        // when & then
        mockMvc.perform(put("/api/blogs/caboom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .content(requestStr)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("메인블로그 변경 실패 - 잘못된 요청")
    void switchMainBlogFail_BadRequest() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.doThrow(new BadRequestException("잘못된 요청"))
                .when(blogService).switchMainBlogTo(anyString(), anyLong());

        // when & then
        mockMvc.perform(put("/api/blogs/caboom/main")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("메인블로그 변경 성공")
    void switchMainBlog() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(testMember);
        Mockito.doNothing()
                .when(blogService).switchMainBlogTo(anyString(), anyLong());

        // when & then
        mockMvc.perform(put("/api/blogs/caboom/main")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200));
    }
}