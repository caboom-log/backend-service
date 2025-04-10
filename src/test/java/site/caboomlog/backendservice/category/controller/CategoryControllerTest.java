package site.caboomlog.backendservice.category.controller;

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
import site.caboomlog.backendservice.category.advice.CategoryControllerAdvice;
import site.caboomlog.backendservice.category.service.CategoryService;
import site.caboomlog.backendservice.common.advice.CommonControllerAdvice;
import site.caboomlog.backendservice.common.annotation.LoginMemberArgumentResolver;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.common.exception.UnauthenticatedException;
import site.caboomlog.backendservice.common.interceptor.AuthHeaderInterceptor;
import site.caboomlog.backendservice.member.entity.Member;
import site.caboomlog.backendservice.member.repository.MemberRepository;
import site.caboomlog.backendservice.topic.advice.TopicControllerAdvice;
import site.caboomlog.backendservice.topic.exception.TopicNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategoryController.class)
@Import(CategoryControllerTest.MockConfig.class)
class CategoryControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    CategoryService categoryService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    LoginMemberArgumentResolver loginMemberArgumentResolver;

    Member testMember = Member.ofExistingMember(
            1L, "caboom@test.com", "카붐", "1234qwer",
            "010-0000-1111", null, null
    );

    @TestConfiguration
    static class MockConfig {
        @Bean
        public CategoryService categoryService() {
            return Mockito.mock(CategoryService.class);
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
                .standaloneSetup(new CategoryController(categoryService))
                .setCustomArgumentResolvers(loginMemberArgumentResolver)
                .addInterceptors(authHeaderInterceptor)
                .setControllerAdvice(TopicControllerAdvice.class, CategoryControllerAdvice.class, CommonControllerAdvice.class)
                .build();
    }

    @Test
    @DisplayName("카테고리 등록 실패 - 토픽 없음")
    void createCategoryFail_TopicNotFound() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.of(testMember));
        Mockito.doThrow(new TopicNotFoundException("토픽을 찾을 수 없습니다"))
                .when(categoryService).createCategory(anyString(), anyLong(), any());

        // when & then
        mockMvc.perform(post("/api/blogs/caboom/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                .content("""
                {
                "categoryPid" : 1,
                "categoryName" : "공부",
                "topicId" : 11,
                "categoryPublic" : true
                }
                """))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("카테고리 등록 실패 - 블로그 소유자 아님")
    void createCategoryFail_Unauthenticated() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.of(testMember));
        Mockito.doThrow(new UnauthenticatedException("블로그 소유자가 아닙니다"))
                .when(categoryService).createCategory(anyString(), anyLong(), any());

        // when & then
        mockMvc.perform(post("/api/blogs/caboom/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .content("""
                {
                "categoryPid" : 1,
                "categoryName" : "공부",
                "topicId" : 11,
                "categoryPublic" : true
                }
                """))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("카테고리 등록 실패 - 최대 depth 초과")
    void createCategoryFail_DepthIsMax() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.of(testMember));
        Mockito.doThrow(new BadRequestException("depth는 최대 5까지만 가능합니다"))
                .when(categoryService).createCategory(anyString(), anyLong(), any());

        // when & then
        mockMvc.perform(post("/api/blogs/caboom/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .content("""
                {
                "categoryPid" : 1,
                "categoryName" : "공부",
                "topicId" : 11,
                "categoryPublic" : true
                }
                """))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("카테고리 등록 성공")
    void createCategorySuccess() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.of(testMember));
        Mockito.doNothing()
                .when(categoryService).createCategory(anyString(), anyLong(), any());

        // when & then
        mockMvc.perform(post("/api/blogs/caboom/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .content("""
                {
                "categoryPid" : 1,
                "categoryName" : "공부",
                "topicId" : 11,
                "categoryPublic" : true
                }
                """))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

}