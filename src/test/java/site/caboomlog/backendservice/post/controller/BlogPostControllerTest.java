package site.caboomlog.backendservice.post.controller;

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
import site.caboomlog.backendservice.blog.dto.TeamBlogMemberResponse;
import site.caboomlog.backendservice.common.advice.CommonControllerAdvice;
import site.caboomlog.backendservice.common.annotation.LoginMemberArgumentResolver;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.common.exception.DatabaseException;
import site.caboomlog.backendservice.common.exception.UnauthenticatedException;
import site.caboomlog.backendservice.common.interceptor.AuthHeaderInterceptor;
import site.caboomlog.backendservice.member.entity.Member;
import site.caboomlog.backendservice.member.repository.MemberRepository;
import site.caboomlog.backendservice.post.advice.PostControllerAdvice;
import site.caboomlog.backendservice.post.dto.PostDetailResponse;
import site.caboomlog.backendservice.post.exception.PostNotFoundException;
import site.caboomlog.backendservice.post.service.BlogPostService;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(BlogPostControllerTest.MockConfig.class)
@WebMvcTest(controllers = BlogPostController.class)
class BlogPostControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    BlogPostService blogPostService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LoginMemberArgumentResolver loginMemberArgumentResolver;

    Member testMember = Member.ofExistingMember(1L, "caboom@test.com", "세연", "qwer7890",
            "010-0000-1111", null, null);

    @TestConfiguration
    static class MockConfig {
        @Bean
        BlogPostService blogPostService() {
            return Mockito.mock(BlogPostService.class);
        }
        @Bean
        MemberRepository memberRepository() {
            return Mockito.mock(MemberRepository.class);
        }
        @Bean
        AuthHeaderInterceptor authHeaderInterceptor() {
            return new AuthHeaderInterceptor();
        }
    }

    @BeforeEach
    void setup(@Autowired HandlerMethodArgumentResolver loginMemberArgumentResolver,
               @Autowired AuthHeaderInterceptor authHeaderInterceptor) {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new BlogPostController(blogPostService))
                .setCustomArgumentResolvers(loginMemberArgumentResolver)
                .addInterceptors(authHeaderInterceptor)
                .setControllerAdvice(
                        PostControllerAdvice.class,
                        CommonControllerAdvice.class)
                .build();
    }

    @BeforeEach
    void resetMocks() {
        Mockito.reset(blogPostService);
    }

    @Test
    @DisplayName("블로그 게시글 작성 실패 - 인증 실패")
    void writePostFail_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/blogs/caboom/posts")
                .content("""
                        {"title" : "테스트 제목",
                        "content" : "안녕하세요~",
                        "categoryIds" : [1,2],
                        "postPublic" : true,
                        "thumbnail" : null
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("블로그 게시글 작성 실패 - 인가 실패")
    void writePostFail_Unauthenticated() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.of(testMember));
        Mockito.doThrow(new UnauthenticatedException("블로그 멤버가 아님"))
                .when(blogPostService).createPost(anyString(), anyLong(), any());
        // when & then
        mockMvc.perform(post("/api/blogs/caboom/posts")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"title" : "테스트 제목",
                        "content" : "안녕하세요~",
                        "categoryIds" : [1,2],
                        "postPublic" : true,
                        "thumbnail" : null}
                        """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("블로그 게시글 작성 실패 - 제목이 비었거나 공백")
    void writePostFail_TitleEmpty() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.of(testMember));
        // when & then
        mockMvc.perform(post("/api/blogs/caboom/posts")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"title" : "   ",
                        "content" : "안녕하세요~",
                        "categoryIds" : [1,2],
                        "postPublic" : true,
                        "thumbnail" : null}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("제목은 필수 입력값입니다.\n"));
    }

    @Test
    @DisplayName("블로그 게시글 작성 실패 - 카테고리 선택 안 했는데 기본카테고리가 생성 안 되어 있을 경우")
    void writePostFail_DatabaseException() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.of(testMember));
        Mockito.doThrow(new DatabaseException(""))
                .when(blogPostService).createPost(anyString(), anyLong(), any());
        // when & then
        mockMvc.perform(post("/api/blogs/caboom/posts")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"title" : "제목",
                        "content" : "안녕하세요~",
                        "categoryIds" : [],
                        "postPublic" : true,
                        "thumbnail" : null}
                        """))
                .andExpect(status().isInternalServerError())
                .andDo(print())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("블로그 게시글 작성 실패 - 잘못된 요청")
    void writePostFail_PublicPostInPrivateCategory() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.of(testMember));
        Mockito.doThrow(new BadRequestException("잘못된 요청"))
                .when(blogPostService).createPost(anyString(), anyLong(), any());
        // when & then
        mockMvc.perform(post("/api/blogs/caboom/posts")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"title" : "제목",
                        "content" : "안녕하세요~",
                        "categoryIds" : [1, 2],
                        "postPublic" : true,
                        "thumbnail" : null}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("블로그 게시글 작성 성공")
    void writePostSuccess() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.of(testMember));
        Mockito.doNothing()
                .when(blogPostService).createPost(anyString(), anyLong(), any());
        // when & then
        mockMvc.perform(post("/api/blogs/caboom/posts")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"title" : "제목",
                        "content" : "안녕하세요~",
                        "categoryIds" : [1, 2],
                        "postPublic" : true,
                        "thumbnail" : null}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("블로그 게시글 단일 조회 실패 - 로그인하지 않은 상태로 비공개 게시글을 조회 시도")
    void getPostDetailFail_Unauthorized() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.empty());
        Mockito.doThrow(new UnauthenticatedException("권한 없음"))
                .when(blogPostService).getPostDetail(anyString(), anyLong(), any());

        // when & then
        mockMvc.perform(get("/api/blogs/caboom/posts/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("블로그 게시글 단일 조회 실패 - 인가되지 않은 회원이 비공개 게시글을 조회 시도")
    void getPostDetailFail_Unauthenticated() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.of(testMember));
        Mockito.doThrow(new UnauthenticatedException("권한 없음"))
                .when(blogPostService).getPostDetail(anyString(), anyLong(), any());

        // when & then
        mockMvc.perform(get("/api/blogs/caboom/posts/1")
                .header("X-Caboomlog-UID", UUID.randomUUID().toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("블로그 게시글 단일 조회 실패 - 존재하지 않는 게시글")
    void getPostDetailFail_PostNotFound() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.of(testMember));
        Mockito.doThrow(new PostNotFoundException("게시글이 없음"))
                .when(blogPostService).getPostDetail(anyString(), anyLong(), any());

        // when & then
        mockMvc.perform(get("/api/blogs/caboom/posts/1")
                .header("X-Caboomlog-UID", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("블로그 게시글 단일 조회 실패 - 게시글이 블로그 소속 게시글이 아닐때")
    void getPostDetailFail_BadRequest() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.of(testMember));
        Mockito.doThrow(new BadRequestException("잘못된 요청입니다."))
                .when(blogPostService).getPostDetail(anyString(), anyLong(), any());

        // when & then
        mockMvc.perform(get("/api/blogs/caboom/posts/1")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("블로그 게시글 단일 조회 성공")
    void getPostDetailSuccess() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.of(testMember));
        Mockito.when(blogPostService.getPostDetail(anyString(), anyLong(), any()))
                .thenReturn(new PostDetailResponse(1L,
                        new TeamBlogMemberResponse(UUID.randomUUID().toString(), "글쓴이", "카붐로그"), null,
                        "제목", "안녕하세요.", true, 1L,
                        null, null, null));

        // when & then
        mockMvc.perform(get("/api/blogs/caboom/posts/1")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.content.title").value("제목"));
    }
}