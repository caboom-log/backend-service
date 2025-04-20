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
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import site.caboomlog.backendservice.blog.dto.TeamBlogMemberResponse;
import site.caboomlog.backendservice.common.advice.CommonControllerAdvice;
import site.caboomlog.backendservice.common.annotation.LoginMemberArgumentResolver;
import site.caboomlog.backendservice.common.interceptor.AuthHeaderInterceptor;
import site.caboomlog.backendservice.member.repository.MemberRepository;
import site.caboomlog.backendservice.post.advice.PostControllerAdvice;
import site.caboomlog.backendservice.post.dto.PostResponse;
import site.caboomlog.backendservice.post.service.PublicPostService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicPostController.class)
@Import(PublicPostControllerTest.MockConfig.class)
class PublicPostControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    PublicPostService publicPostService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LoginMemberArgumentResolver loginMemberArgumentResolver;

    TeamBlogMemberResponse memberResponse1 = new TeamBlogMemberResponse(
            UUID.randomUUID().toString(), "세연", "caboom");
    TeamBlogMemberResponse memberResponse2 = new TeamBlogMemberResponse(
            UUID.randomUUID().toString(), "곰새", "gomsae");

    @TestConfiguration
    public static class MockConfig {
        @Bean
        MemberRepository memberRepository() {
            return Mockito.mock(MemberRepository.class);
        }
        @Bean
        AuthHeaderInterceptor authHeaderInterceptor() {
            return new AuthHeaderInterceptor();
        }
        @Bean
        PublicPostService publicPostService() {
            return Mockito.mock(PublicPostService.class);
        }
    }

    @BeforeEach
    void setup(@Autowired HandlerMethodArgumentResolver loginMemberArgumentResolver,
               @Autowired AuthHeaderInterceptor authHeaderInterceptor) {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PublicPostController(publicPostService))
                .setCustomArgumentResolvers(loginMemberArgumentResolver,
                        new PageableHandlerMethodArgumentResolver())
                .addInterceptors(authHeaderInterceptor)
                .setControllerAdvice(
                        PostControllerAdvice.class,
                        CommonControllerAdvice.class)
                .build();
    }

    @BeforeEach
    void resetMocks() {
        Mockito.reset(publicPostService);
    }

    @Test
    @DisplayName("블로그 공개 게시물 목록 조회 성공 - 게시글이 없을때")
    void getAllPublicBlogPosts() throws Exception {
        // given
        Page<PostResponse> emptyPage = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")),
                0);

        Mockito.when(publicPostService.getAllPublicPosts(eq("caboom"), any(Pageable.class)))
                .thenReturn(emptyPage);

        // when & then
        mockMvc.perform(get("/api/blogs/caboom/posts/public")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("size", "5")
                        .param("page", "0")
                        .param("sort", "createdAt, desc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.content.posts").isEmpty())
                .andExpect(jsonPath("$.content.totalElements").value(0));
    }

    @Test
    @DisplayName("블로그 공개 게시물 목록 조회 성공")
    void getAllPublicBlogPostsSuccess() throws Exception {
        // given
        Page<PostResponse> page = new PageImpl<>(
                List.of(
                        new PostResponse(1L, "caboom", "제목2", memberResponse1, "안녕하세요2",
                        null, null, null, 11L, List.of("일상", "여행"))),
                PageRequest.of(2, 1, Sort.by(Sort.Direction.DESC, "createdAt")),
                3);

        Mockito.when(publicPostService.getAllPublicPosts(eq("caboom"), any(Pageable.class)))
                .thenReturn(page);

        // when & then
        mockMvc.perform(get("/api/blogs/caboom/posts/public")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("size", "1")
                        .param("page", "2")
                        .param("sort", "createdAt, desc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.content.posts").isArray())
                .andExpect(jsonPath("$.content.totalElements").value(3))
                .andExpect(jsonPath("$.content.posts[0].title").value("제목2"));
    }

    @Test
    @DisplayName("블로그 공개 게시물 목록 조회 성공 - 파라미터 없으면 size=5, offset=0")
    void getAllPublicBlogPostsSuccess_NoParameter() throws Exception {
        // given
        Page<PostResponse> emptyPage = new PageImpl<>(
                List.of(
                        new PostResponse(1L, "caboom", "제목2", memberResponse1, "안녕하세요2",
                                null, null, null, 11L, List.of("일상", "여행"))),
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")),
                280);

        Mockito.when(publicPostService.getAllPublicPosts(eq("caboom"), any(Pageable.class)))
                .thenReturn(emptyPage);

        // when & then
        mockMvc.perform(get("/api/blogs/caboom/posts/public")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.content.posts").isArray())
                .andExpect(jsonPath("$.content.totalElements").value(280))
                .andExpect(jsonPath("$.content.posts[0].title").value("제목2"));
    }

    @Test
    @DisplayName("전체 공개 게시물 목록 조회")
    void getAllPublicPosts() throws Exception {
        // given
        PageImpl<PostResponse> result = new PageImpl<>(
                List.of(new PostResponse(1L, "caboom", "제목1",
                        new TeamBlogMemberResponse(UUID.randomUUID().toString(), "주인", "hello1234"),
                        "안녕하세요 날씨가 아주 좋네요", null, null, null,10L,
                        List.of("일상")),
                        new PostResponse(2L, "caboom", "제목2",
                                new TeamBlogMemberResponse(UUID.randomUUID().toString(), "주인", "hello1234"),
                                "안녕하세요 2트", null, null, null, 10L,
                                List.of("일상")),
                        new PostResponse(3L, "caboom", "제목3",
                                new TeamBlogMemberResponse(UUID.randomUUID().toString(), "주인", "hello1234"),
                                "안녕하세요 3트", null, null, null, 10L,
                                List.of("일상")),
                        new PostResponse(4L, "caboom", "제목4",
                                new TeamBlogMemberResponse(UUID.randomUUID().toString(), "주인", "hello1234"),
                                "안녕하세요 4트", null, null, null,10L,
                                List.of("일상")),
                        new PostResponse(5L, "caboom", "제목5",
                                new TeamBlogMemberResponse(UUID.randomUUID().toString(), "주인", "hello1234"),
                                "안녕하세요 5트", null, null, null, 10L,
                                List.of("일상"))
                ),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")),
                5);
        Mockito.when(publicPostService.getAllPublicPosts(any()))
                        .thenReturn(result);

        // when & then
        mockMvc.perform(get("/api/posts/public")
                .param("size", "10")
                .param("pate", "0")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.posts[1].title").value("제목2"));
    }
}