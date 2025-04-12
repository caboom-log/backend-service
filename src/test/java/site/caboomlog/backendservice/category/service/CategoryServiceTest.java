package site.caboomlog.backendservice.category.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import site.caboomlog.backendservice.blog.entity.Blog;
import site.caboomlog.backendservice.blog.entity.BlogType;
import site.caboomlog.backendservice.blogmember.entity.BlogMemberMapping;
import site.caboomlog.backendservice.blogmember.repository.BlogMemberMappingRepository;
import site.caboomlog.backendservice.category.dto.CategoryResponse;
import site.caboomlog.backendservice.category.dto.CreateCategoryRequest;
import site.caboomlog.backendservice.category.entity.Category;
import site.caboomlog.backendservice.category.exception.CategoryNotFoundException;
import site.caboomlog.backendservice.category.repository.CategoryRepository;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.common.exception.UnauthenticatedException;
import site.caboomlog.backendservice.member.entity.Member;
import site.caboomlog.backendservice.role.entity.Role;
import site.caboomlog.backendservice.topic.entity.Topic;
import site.caboomlog.backendservice.topic.exception.TopicNotFoundException;
import site.caboomlog.backendservice.topic.repository.TopicRepository;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;


@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @Mock
    BlogMemberMappingRepository blogMemberMappingRepository;
    @Mock
    TopicRepository topicRepository;
    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    CategoryService categoryService;

    @Captor
    ArgumentCaptor<Category> categoryCaptor;

    Member testMember = Member.ofExistingMember(1L, "caboom@test.com", "caboom",
            "1234qwer", "010-0000-1111", null, null);
    Blog testBlog = Blog.ofExistingBlog(1L, "caboom", true, "카붐로그",
            "안녕하세요", true, null, BlogType.PERSONAL);
    Role roleOwner = Role.ofNewRole("ROLE_OWNER", "블로그_소유자", "블로그 소유자 입니다.");
    Topic testTopic = Topic.ofExsitingTopic(1, null, "여행",
            1, null, null);

    Field categoryPidField;
    Field categoryNameField;
    Field topicIdField;
    Field categoryPublicField;

    @BeforeEach
    void setUp() throws Exception {
        categoryPidField = CreateCategoryRequest.class.getDeclaredField("categoryPid");
        categoryPidField.setAccessible(true);
        categoryNameField = CreateCategoryRequest.class.getDeclaredField("categoryName");
        categoryNameField.setAccessible(true);
        topicIdField = CreateCategoryRequest.class.getDeclaredField("topicId");
        topicIdField.setAccessible(true);
        categoryPublicField = CreateCategoryRequest.class.getDeclaredField("categoryPublic");
        categoryPublicField.setAccessible(true);
    }

    @Test
    @DisplayName("카테고리 등록 실패 - 블로그 소유자가 아님")
    void createCategoryFail_Unauthenticated() throws Exception {
        // given
        Constructor<CreateCategoryRequest> categoryRequestAllArgsConstructor =
                CreateCategoryRequest.class.getDeclaredConstructor();
        categoryRequestAllArgsConstructor.setAccessible(true);
        CreateCategoryRequest request = categoryRequestAllArgsConstructor.newInstance();

        categoryNameField.set(request, "루트카테고리");
        topicIdField.set(request, 1);
        categoryPublicField.set(request, true);

        Mockito.when(blogMemberMappingRepository
                .findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenThrow(new UnauthenticatedException("블로그 소유자가 아닙니다."));

        // when & then
        Assertions.assertThrows(UnauthenticatedException.class,
                () -> categoryService.createCategory("caboom", 2L, request));
    }

    @Test
    @DisplayName("카테고리 등록 실패 - 해당 토픽이 없음")
    void createCategoryFail_TopicNotFound() throws Exception {
        // given
        Constructor<CreateCategoryRequest> categoryRequestAllArgsConstructor =
                CreateCategoryRequest.class.getDeclaredConstructor();
        categoryRequestAllArgsConstructor.setAccessible(true);
        CreateCategoryRequest request = categoryRequestAllArgsConstructor.newInstance();

        categoryNameField.set(request, "루트카테고리");
        topicIdField.set(request, 1);
        categoryPublicField.set(request, true);

        Mockito.when(blogMemberMappingRepository
                        .findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(
                        testBlog, testMember, roleOwner, "블로그 소유자"
                ));
        Mockito.when(topicRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        // when & then
        Assertions.assertThrows(TopicNotFoundException.class,
                () -> categoryService.createCategory("caboom", 1L, request));
    }

    @Test
    @DisplayName("카테고리 등록 실패 - 상위카테고리를 입력하였지만 해당 카테고리가 존재하지 않음")
    void createCategoryFail_ParentCategoryNotFound() throws Exception {
        // given
        Constructor<CreateCategoryRequest> categoryRequestAllArgsConstructor =
                CreateCategoryRequest.class.getDeclaredConstructor();
        categoryRequestAllArgsConstructor.setAccessible(true);
        CreateCategoryRequest request = categoryRequestAllArgsConstructor.newInstance();

        categoryNameField.set(request, "루트카테고리");
        topicIdField.set(request, 1);
        categoryPublicField.set(request, true);
        categoryPidField.set(request, 222L);

        Mockito.when(blogMemberMappingRepository
                        .findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(
                        testBlog, testMember, roleOwner, "블로그 소유자"
                ));
        Mockito.when(topicRepository.findById(anyInt()))
                .thenReturn(Optional.of(testTopic));
        Mockito.when(categoryRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // when & then
        Assertions.assertThrows(CategoryNotFoundException.class,
                () -> categoryService.createCategory("caboom", 1L, request));
    }

    @Test
    @DisplayName("카테고리 등록 실패 - 카테고리 최대 depth 초과")
    void createCategoryFail_MaxDepthOver() throws Exception {
        // given
        Constructor<CreateCategoryRequest> categoryRequestAllArgsConstructor =
                CreateCategoryRequest.class.getDeclaredConstructor();
        categoryRequestAllArgsConstructor.setAccessible(true);
        CreateCategoryRequest request = categoryRequestAllArgsConstructor.newInstance();

        categoryNameField.set(request, "루트카테고리");
        topicIdField.set(request, 1);
        categoryPublicField.set(request, true);
        categoryPidField.set(request, 22L);

        Mockito.when(blogMemberMappingRepository
                        .findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(
                        testBlog, testMember, roleOwner, "블로그 소유자"
                ));
        Mockito.when(topicRepository.findById(anyInt()))
                .thenReturn(Optional.of(testTopic));
        Mockito.when(categoryRepository.findById(anyLong()))
                .thenReturn(Optional.of(
                        Category.ofNewCategory(testBlog, null, testTopic, "depth5인 카테고리",
                                true, 1, 5)
                ));

        // when & then
        Assertions.assertThrows(BadRequestException.class,
                () -> categoryService.createCategory("caboom", 1L, request));
    }

    @Test
    @DisplayName("카테고리 등록 실패 - 카테고리 최대 갯수 초과")
    void createCategoryFail_MaxCountOver() throws Exception {
        // given
        Constructor<CreateCategoryRequest> categoryRequestAllArgsConstructor =
                CreateCategoryRequest.class.getDeclaredConstructor();
        categoryRequestAllArgsConstructor.setAccessible(true);
        CreateCategoryRequest request = categoryRequestAllArgsConstructor.newInstance();

        categoryNameField.set(request, "루트카테고리");
        topicIdField.set(request, 1);
        categoryPublicField.set(request, true);
        categoryPidField.set(request, 22L);

        Mockito.when(blogMemberMappingRepository
                        .findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(
                        testBlog, testMember, roleOwner, "블로그 소유자"
                ));
        Mockito.when(topicRepository.findById(anyInt()))
                .thenReturn(Optional.of(testTopic));
        Mockito.when(categoryRepository.countByBlog_BlogFid(anyString())).thenReturn(200);

        // when & then
        Assertions.assertThrows(BadRequestException.class,
                () -> categoryService.createCategory("caboom", 1L, request));
    }

    @Test
    @DisplayName("상위 카테고리가 private이고 등록할 하위 카테고리가 public이면 BadRequest")
    void createCaetgoryFail_CreatePublicUnderPrivate() throws Exception {
        // given
        Constructor<CreateCategoryRequest> categoryRequestAllArgsConstructor =
                CreateCategoryRequest.class.getDeclaredConstructor();
        categoryRequestAllArgsConstructor.setAccessible(true);
        CreateCategoryRequest request = categoryRequestAllArgsConstructor.newInstance();

        categoryNameField.set(request, "루트카테고리");
        topicIdField.set(request, 1);
        categoryPublicField.set(request, true);
        categoryPidField.set(request, 22L);

        Mockito.when(blogMemberMappingRepository
                        .findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(
                        testBlog, testMember, roleOwner, "블로그 소유자"
                ));
        Mockito.when(topicRepository.findById(anyInt()))
                .thenReturn(Optional.of(testTopic));
        Mockito.when(categoryRepository.findById(anyLong()))
                .thenReturn(Optional.of(
                        Category.ofNewCategory(testBlog, null, testTopic, "depth5인 카테고리",
                                false, 1, 5)
                ));

        // when & then
        Assertions.assertThrows(BadRequestException.class,
                () -> categoryService.createCategory("caboom", 1L, request));
    }

    @Test
    @DisplayName("root 카테고리 등록 성공")
    void createCaetgorySuccess_root() throws Exception {
        // given
        Constructor<CreateCategoryRequest> categoryRequestAllArgsConstructor =
                CreateCategoryRequest.class.getDeclaredConstructor();
        categoryRequestAllArgsConstructor.setAccessible(true);
        CreateCategoryRequest request = categoryRequestAllArgsConstructor.newInstance();

        categoryNameField.set(request, "루트카테고리");
        topicIdField.set(request, 1);
        categoryPublicField.set(request, true);

        Mockito.when(blogMemberMappingRepository
                        .findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(
                        testBlog, testMember, roleOwner, "블로그 소유자"
                ));
        Mockito.when(topicRepository.findById(anyInt()))
                .thenReturn(Optional.of(testTopic));
        Mockito.when(categoryRepository.countByBlog_BlogFidAndParentCategory(anyString(), any()))
                .thenReturn(3);

        // when
        categoryService.createCategory("caboom", 1L, request);

        // then
        Mockito.verify(categoryRepository).save(categoryCaptor.capture());
        Category saved = categoryCaptor.getValue();

        Assertions.assertEquals("루트카테고리", saved.getCategoryName());
        Assertions.assertEquals(4, saved.getCategoryOrder());
        Assertions.assertEquals(1, saved.getDepth());
        Assertions.assertNull(saved.getParentCategory());
        Assertions.assertTrue(saved.getCategoryPublic());
    }

    @Test
    @DisplayName("sub 카테고리 등록 성공")
    void createCaetgorySuccess_sub() throws Exception {
        // given
        Constructor<CreateCategoryRequest> categoryRequestAllArgsConstructor =
                CreateCategoryRequest.class.getDeclaredConstructor();
        categoryRequestAllArgsConstructor.setAccessible(true);
        CreateCategoryRequest request = categoryRequestAllArgsConstructor.newInstance();
        Category parent = Category.ofNewCategory(testBlog, null, testTopic, "부모",
                true, 1, 2);

        categoryNameField.set(request, "서브카테고리");
        topicIdField.set(request, 1);
        categoryPublicField.set(request, true);
        categoryPidField.set(request, 1L);

        Mockito.when(blogMemberMappingRepository
                        .findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(
                        testBlog, testMember, roleOwner, "블로그 소유자"
                ));
        Mockito.when(topicRepository.findById(anyInt()))
                .thenReturn(Optional.of(testTopic));
        Mockito.when(categoryRepository.findById(anyLong()))
                        .thenReturn(Optional.of(parent));

        // when
        categoryService.createCategory("caboom", 1L, request);

        // then
        Mockito.verify(categoryRepository).save(categoryCaptor.capture());
        Category saved = categoryCaptor.getValue();

        Assertions.assertEquals("서브카테고리", saved.getCategoryName());
        Assertions.assertEquals(1, saved.getCategoryOrder());
        Assertions.assertEquals(3, saved.getDepth());
        Assertions.assertEquals(parent, saved.getParentCategory());
        Assertions.assertTrue(saved.getCategoryPublic());
    }

    @Test
    @DisplayName("카테고리 전체 조회 실패 - 블로그 소유자가 아님")
    void getCategoriesFail_Unauthenticated() {
        // given
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(
                        testBlog, testMember, Role.ofNewRole("ROLE_MEMBER", "블로그_멤버", ""),
                        "멤버1"
        ));

        // when & then
        Assertions.assertThrows(UnauthenticatedException.class,
                () -> categoryService.getCategories("caboom", 2L));
    }

    @Test
    @DisplayName("카테고리 전체 조회 성공")
    void getCategories() {
        // given
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(
                        testBlog, testMember, roleOwner, "멤버1"));
        Mockito.when(categoryRepository.findAllByBlog_BlogFid(anyString()))
                .thenReturn(List.of(
                        Category.ofNewCategory(testBlog, null, testTopic,
                                "root", true, 1, 1)
        ));
        // when
        List<CategoryResponse> response = categoryService.getCategories("caboom", 2L);

        // then
        Assertions.assertEquals(1, response.size());
    }

    @DisplayName("공개 카테고리 전체 조회 성공 - 트리 구조로 리턴")
    @Test
    void getPublicCategoriesSuccess() throws Exception {
        // given
        Category root1 = Category.ofNewCategory(testBlog, null, testTopic,
                "루트1", true, 1, 1);
        Category root2 = Category.ofNewCategory(testBlog, null, testTopic,
                "루트2", true, 2, 1);

        Category sub1_1 = Category.ofNewCategory(testBlog, root1, testTopic,
                "서브1-1", true, 1, 2);
        Category sub1_2 = Category.ofNewCategory(testBlog, root1, testTopic,
                "서브1-2", true, 2, 2);
        Category sub2_1 = Category.ofNewCategory(testBlog, root2, testTopic,
                "서브2-1", true, 1, 2);

        Field categoryIdField = Category.class.getDeclaredField("categoryId");
        categoryIdField.setAccessible(true);
        categoryIdField.set(root1, 1L);
        categoryIdField.set(root2, 2L);
        categoryIdField.set(sub1_1, 3L);
        categoryIdField.set(sub1_2, 4L);
        categoryIdField.set(sub2_1, 5L);

        Mockito.when(categoryRepository.findAllPublicByBlog_BlogFid(anyString()))
                .thenReturn(List.of(root1, root2, sub1_1, sub1_2, sub2_1));

        // when
        List<CategoryResponse> response = categoryService.getAllPublicCategories("caboom");

        // then
        Assertions.assertEquals(2, response.size());

        CategoryResponse r1 = response.get(0);
        Assertions.assertEquals("루트1", r1.getCategoryName());
        Assertions.assertEquals(2, r1.getChildren().size());
        Assertions.assertEquals("서브1-1", r1.getChildren().get(0).getCategoryName());

        CategoryResponse r2 = response.get(1);
        Assertions.assertEquals("루트2", r2.getCategoryName());
        Assertions.assertEquals(1, r2.getChildren().size());
        Assertions.assertEquals("서브2-1", r2.getChildren().get(0).getCategoryName());
    }

    @Test
    @DisplayName("카테고리 공개 여부 변경 실패 - 블로그 소유자가 아님")
    void changeVisibilityFail_Unauthenticated() {
        // given
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(
                        testBlog, testMember,
                        Role.ofNewRole("ROLE_MEMBER", "블로그_멤버", "멤버"),
                        "멤버"
                ));
        // when & then
        Assertions.assertThrows(UnauthenticatedException.class,
                () -> categoryService.changeVisibility(1L, "caboom", 1L, true));

    }

    @Test
    @DisplayName("카테고리 공개 여부 변경 실패 - 카테고리가 블로그에 소속되지 않음")
    void changeVisibilityFail_CategoryNotBelongsToBlog() {
        // given
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(testBlog, testMember, roleOwner, "멤버"));
        Mockito.when(categoryRepository.findByCategoryId(anyLong()))
                .thenReturn(Optional.of(Category.ofNewCategory(
                        Blog.ofNewBlog("다른블로그", true, "다른블로그", null,
                                false, BlogType.PERSONAL),
                        null, testTopic, "카테고리!", true,
                        1, 1)));

        // when & then
        Assertions.assertThrows(BadRequestException.class,
                () -> categoryService.changeVisibility(1L, "caboom", 1L, true));
    }

    @Test
    @DisplayName("카테고리 공개 여부 변경 성공")
    void changeVisibilitySuccess() {
        // given
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(testBlog, testMember, roleOwner, "멤버"));
        Mockito.when(categoryRepository.findByCategoryId(anyLong()))
                .thenReturn(Optional.of(Category.ofNewCategory(testBlog, null, testTopic,
                        "카테고리!", false, 1, 1)));

        // when
        categoryService.changeVisibility(1L, "caboom", 1L, true);
        Mockito.verify(categoryRepository).save(categoryCaptor.capture());
        Category saved = categoryCaptor.getValue();

        // then
        Assertions.assertTrue(saved.getCategoryPublic());
    }

    @Test
    @DisplayName("카테고리 공개 여부 변경 성공 - 상위 카테고리 private으로 변경시 하위 카테고리도 모두 private으로 변경")
    void changeVisibilitySuccess_childrenPrivate() throws Exception {
        // given
        Category parent = Category.ofNewCategory(testBlog, null, testTopic, "루트1",
                true, 1, 1);
        Category child1 = Category.ofNewCategory(testBlog, parent, testTopic, "자식1",
                true, 1, 2);
        Category child2 = Category.ofNewCategory(testBlog, parent, testTopic, "자식2",
                true, 2, 2);
        Field categoryIdField = Category.class.getDeclaredField("categoryId");
        categoryIdField.setAccessible(true);
        categoryIdField.set(parent, 1L);
        categoryIdField.set(child1, 2L);
        categoryIdField.set(child2, 3L);
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(testBlog, testMember, roleOwner, "멤버"));
        Mockito.when(categoryRepository.findByCategoryId(anyLong()))
                .thenReturn(Optional.of(parent));
        Mockito.when(categoryRepository.findAllByBlog_BlogFid(anyString()))
                .thenReturn(List.of(parent, child1, child2));

        // when
        categoryService.changeVisibility(testMember.getMbNo(), testBlog.getBlogFid(), parent.getCategoryId(),
                false);

        // then
        Assertions.assertFalse(parent.getCategoryPublic());
        Assertions.assertFalse(child1.getCategoryPublic());
        Assertions.assertFalse(child2.getCategoryPublic());
    }

    @Test
    @DisplayName("카테고리 공개 여부 변경 성공 - 상위 카테고리 public으로 변경시 하위 카테고리는 변경x")
    void changeVisibilitySuccess_parentPublic() throws Exception {
        // given
        Category parent = Category.ofNewCategory(testBlog, null, testTopic, "루트1",
                false, 1, 1);
        Category child1 = Category.ofNewCategory(testBlog, parent, testTopic, "자식1",
                false, 1, 2);
        Category child2 = Category.ofNewCategory(testBlog, parent, testTopic, "자식2",
                false, 2, 2);
        Field categoryIdField = Category.class.getDeclaredField("categoryId");
        categoryIdField.setAccessible(true);
        categoryIdField.set(parent, 1L);
        categoryIdField.set(child1, 2L);
        categoryIdField.set(child2, 3L);
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(testBlog, testMember, roleOwner, "소유자"));
        Mockito.when(categoryRepository.findByCategoryId(anyLong()))
                .thenReturn(Optional.of(parent));

        // when
        categoryService.changeVisibility(testMember.getMbNo(), testBlog.getBlogFid(), parent.getCategoryId(),
                true);

        // then
        Assertions.assertTrue(parent.getCategoryPublic());
        Assertions.assertFalse(child1.getCategoryPublic());
        Assertions.assertFalse(child2.getCategoryPublic());
        Mockito.verify(categoryRepository, Mockito.times(1)).save(any(Category.class));
        Mockito.verify(categoryRepository, Mockito.times(0)).findAllByBlog_BlogFid(anyString());
    }

    @Test
    @DisplayName("카테고리 순서 변경 실패 - 서로 다른 depth의 카테고리")
    void switchOrderFail_DifferentDepth() {
        // given
        Category category1 = Category.ofNewCategory(testBlog, null, testTopic, "루트",
                true, 1, 1);
        Category category2 = Category.ofNewCategory(testBlog, category1, testTopic, "서브",
                true, 1, 2);
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(testBlog, testMember, roleOwner, "소유자"));
        Mockito.when(categoryRepository.findByCategoryId(1L)).thenReturn(Optional.of(category1));
        Mockito.when(categoryRepository.findByCategoryId(2L)).thenReturn(Optional.of(category2));

        // when & then
        Assertions.assertThrows(BadRequestException.class,
                () -> categoryService.switchOrder("caboom", 1L, 1L, 2L));
    }

    @Test
    @DisplayName("카테고리 순서 변경 성공")
    void switchOrderSuccess() {
        // given
        Category category1 = Category.ofNewCategory(testBlog, null, testTopic, "루트1",
                true, 1, 1);
        Category category2 = Category.ofNewCategory(testBlog, category1, testTopic, "루트2",
                true, 2, 1);
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(testBlog, testMember, roleOwner, "소유자"));
        Mockito.when(categoryRepository.findByCategoryId(1L)).thenReturn(Optional.of(category1));
        Mockito.when(categoryRepository.findByCategoryId(2L)).thenReturn(Optional.of(category2));

        // when
        categoryService.switchOrder("caboom", 1L, 1L, 2L);

        // then
        Assertions.assertEquals(2, category1.getCategoryOrder());
        Assertions.assertEquals(1, category2.getCategoryOrder());
    }
}