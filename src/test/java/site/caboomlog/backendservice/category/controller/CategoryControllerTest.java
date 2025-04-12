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
import site.caboomlog.backendservice.blog.entity.Blog;
import site.caboomlog.backendservice.blog.entity.BlogType;
import site.caboomlog.backendservice.category.advice.CategoryControllerAdvice;
import site.caboomlog.backendservice.category.dto.CategoryResponse;
import site.caboomlog.backendservice.category.entity.Category;
import site.caboomlog.backendservice.category.exception.CategoryNotFoundException;
import site.caboomlog.backendservice.category.service.CategoryService;
import site.caboomlog.backendservice.common.advice.CommonControllerAdvice;
import site.caboomlog.backendservice.common.annotation.LoginMemberArgumentResolver;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.common.exception.UnauthenticatedException;
import site.caboomlog.backendservice.common.interceptor.AuthHeaderInterceptor;
import site.caboomlog.backendservice.member.entity.Member;
import site.caboomlog.backendservice.member.repository.MemberRepository;
import site.caboomlog.backendservice.topic.advice.TopicControllerAdvice;
import site.caboomlog.backendservice.topic.entity.Topic;
import site.caboomlog.backendservice.topic.exception.TopicNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    Blog testBlog = Blog.ofExistingBlog(1L, "caboom", true, "카붐로그",
            "안녕하세요", true, null, BlogType.PERSONAL);
    Topic testTopic = Topic.ofExsitingTopic(1, null, "루트 테스트 토픽", 1,
            null, null);

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
                .setControllerAdvice(TopicControllerAdvice.class,
                        CategoryControllerAdvice.class,
                        CommonControllerAdvice.class)
                .build();
    }

    @BeforeEach
    void resetMocks() {
        Mockito.reset(categoryService);
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
    @DisplayName("카테고리 등록 실패 - 최대 depth 초과 or 비공개 카테고리 하위에 공개카테고리 등록 시도")
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

    @Test
    @DisplayName("카테고리 목록 조회 실패 - 소유자 권한 없음")
    void getAllCategoriesFail_Unauthenticated() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.of(testMember));
        Mockito.when(categoryService.getCategories(anyString(), anyLong()))
                .thenThrow(new UnauthenticatedException("블로그 소유자가 아닙니다."));

        // when & then
        mockMvc.perform(get("/api/blogs/caboom/categories")
                .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("카테고리 목록 조회 성공 - 카테고리가 없을 때")
    void getAllCategoriesSuccess_Empty() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.of(testMember));
        Mockito.when(categoryService.getCategories(anyString(), anyLong()))
                .thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/blogs/caboom/categories")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("카테고리 목록 조회 성공")
    void getAllCategoriesSuccess() throws Exception {
        // given
        Category rootCategory1 = Category.ofNewCategory(testBlog, null, testTopic,
                "루트카테고리1", true, 1, 1);
        Category subCategory1 = Category.ofNewCategory(testBlog, rootCategory1, testTopic,
                "서브카테고리1", false, 1, 2);
        Category subCategory2 = Category.ofNewCategory(testBlog, rootCategory1, testTopic,
                "서브카테고리2", true, 2, 2);

        CategoryResponse root = new CategoryResponse(rootCategory1);
        root.getChildren().add(new CategoryResponse(subCategory1));
        root.getChildren().add(new CategoryResponse(subCategory2));

        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.of(testMember));
        Mockito.when(categoryService.getCategories(anyString(), anyLong()))
                .thenReturn(List.of(
                        root
                ));

        // when & then
        mockMvc.perform(get("/api/blogs/caboom/categories")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].categoryName").value(rootCategory1.getCategoryName()))
                .andExpect(jsonPath("$.content[0].children", hasSize(2)));
    }

    @Test
    @DisplayName("공개 카테고리 목록 조회 성공")
    void getAllPublicCategories() throws Exception {
        // given
        Category rootCategory1 = Category.ofNewCategory(testBlog, null, testTopic,
                "루트카테고리1", true, 1, 1);
        Category subPrivateCategory = Category.ofNewCategory(testBlog, rootCategory1, testTopic,
                "서브카테고리1", false, 1, 2);
        Category subPublicCategory = Category.ofNewCategory(testBlog, rootCategory1, testTopic,
                "서브카테고리2", true, 2, 2);
        CategoryResponse root = new CategoryResponse(rootCategory1);
        root.getChildren().add(new CategoryResponse(subPublicCategory));

        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.of(testMember));
        Mockito.when(categoryService.getAllPublicCategories(anyString()))
                .thenReturn(List.of(root));

        // when & then
        mockMvc.perform(get("/api/blogs/caboom/categories/public")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].categoryName").value(rootCategory1.getCategoryName()))
                .andExpect(jsonPath("$.content[0].children", hasSize(1)));
    }

    @Test
    @DisplayName("카테고리 공개로 변경 실패 - 블로그 소유자가 아님")
    void changeVisibilityFail_Unauthenticated() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.of(testMember));
        Mockito.doThrow(new UnauthenticatedException("블로그 소유자가 아닙니다."))
                .when(categoryService).changeVisibility(anyLong(), anyString(), anyLong(), anyBoolean());

        // when & then
        mockMvc.perform(post("/api/blogs/caboom/categories/1/public")
                .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"categoryPublic\" : true}"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("카테고리 비공개로 변경 실패 - 블로그 소속 카테고리가 아님")
    void changeVisibilityFail_NotBelongsToBlog() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.of(testMember));
        Mockito.doThrow(new BadRequestException("해당 카테고리는 해당 블로그 카테고리가 아닙니다."))
                .when(categoryService).changeVisibility(anyLong(), anyString(), anyLong(), anyBoolean());

        // when & then
        mockMvc.perform(post("/api/blogs/caboom/categories/1/public")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryPublic\" : true}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("카테고리 비공개로 변경 실패 - 카테고리가 존재하지 않음")
    void changeVisibilityFail_CategoryNotFound() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.of(testMember));
        Mockito.doThrow(new CategoryNotFoundException("카테고리가 존재하지 않습니다."))
                .when(categoryService).changeVisibility(anyLong(), anyString(), anyLong(), anyBoolean());

        // when & then
        mockMvc.perform(post("/api/blogs/caboom/categories/1/public")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryPublic\" : true}"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("카테고리 공개로 변경 성공")
    void changeVisibilitySuccess() throws Exception {
        // given
        Mockito.when(memberRepository.findByMbUuid(anyString())).thenReturn(Optional.of(testMember));
        Mockito.doNothing()
                .when(categoryService).changeVisibility(anyLong(), anyString(), anyLong(), anyBoolean());

        // when & then
        mockMvc.perform(post("/api/blogs/caboom/categories/1/public")
                        .header("X-Caboomlog-UID", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryPublic\" : true}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}