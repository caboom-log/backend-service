package site.caboomlog.backendservice.post.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import site.caboomlog.backendservice.blog.dto.TeamBlogMemberResponse;
import site.caboomlog.backendservice.blog.entity.Blog;
import site.caboomlog.backendservice.blog.entity.BlogType;
import site.caboomlog.backendservice.blogmember.entity.BlogMemberMapping;
import site.caboomlog.backendservice.blogmember.repository.BlogMemberMappingRepository;
import site.caboomlog.backendservice.category.entity.Category;
import site.caboomlog.backendservice.category.repository.CategoryRepository;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.common.exception.DatabaseException;
import site.caboomlog.backendservice.common.exception.UnauthenticatedException;
import site.caboomlog.backendservice.member.entity.Member;
import site.caboomlog.backendservice.post.dto.CreatePostRequest;
import site.caboomlog.backendservice.post.dto.PostDetailResponse;
import site.caboomlog.backendservice.post.entity.Post;
import site.caboomlog.backendservice.post.entity.PostCategoryMapping;
import site.caboomlog.backendservice.post.exception.PostNotFoundException;
import site.caboomlog.backendservice.post.repository.PostCategoryMappingRepository;
import site.caboomlog.backendservice.post.repository.PostRepository;
import site.caboomlog.backendservice.post.repository.PostRepositoryImpl;
import site.caboomlog.backendservice.role.entity.Role;
import site.caboomlog.backendservice.topic.entity.Topic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class BlogPostServiceTest {
    @Mock
    PostRepository postRepository;
    @Mock
    BlogMemberMappingRepository blogMemberMappingRepository;
    @Mock
    CategoryRepository categoryRepository;
    @Mock
    PostCategoryMappingRepository postCategoryMappingRepository;
    @Mock
    PostRepositoryImpl postRepositoryCustom;
    @InjectMocks
    BlogPostService blogPostService;
    @Captor
    ArgumentCaptor<Post> postArgumentCaptor;
    @Captor
    ArgumentCaptor<PostCategoryMapping> postCategoryMappingArgumentCaptor;

    CreatePostRequest request;
    Field titleField;
    Field contentField;
    Field categoryIdsField;
    Field postPublicField;
    Field thumbnailField;

    Blog testBlog = Blog.ofExistingBlog(1L, "caboom", true, "카붐로그",
            "안녕하세요", true, null, BlogType.TEAM);
    Role roleOwner = Role.ofNewRole("ROLE_OWNER", "블로그_소유자", "");
    Member testMember = Member.ofExistingMember(1L, "caboom@test.com", "세연", "1234",
            "010-0000-0000", null, null);
    Topic testTopic = Topic.ofExsitingTopic(1, null, "토픽", 1, null, null);
    Category testPublicCategory = Category.ofNewCategory(testBlog, null, testTopic, "테스트카테고리",
            true, 1, 1);
    Category testPrivateCategory = Category.ofNewCategory(testBlog, null, testTopic, "테스트카테고리",
            false, 2, 1);

    @BeforeEach
    void setup() throws Exception {
        Constructor<CreatePostRequest> constructor = CreatePostRequest.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        request = constructor.newInstance();
        titleField = CreatePostRequest.class.getDeclaredField("title");
        titleField.setAccessible(true);
        contentField = CreatePostRequest.class.getDeclaredField("content");
        contentField.setAccessible(true);
        categoryIdsField = CreatePostRequest.class.getDeclaredField("categoryIds");
        categoryIdsField.setAccessible(true);
        postPublicField = CreatePostRequest.class.getDeclaredField("postPublic");
        postPublicField.setAccessible(true);
        thumbnailField = CreatePostRequest.class.getDeclaredField("thumbnail");
        thumbnailField.setAccessible(true);

        titleField.set(request, "제목");
        contentField.set(request, "콘텐츠");
        categoryIdsField.set(request, List.of(1L, 2L));
        postPublicField.set(request, true);
    }

    @Test
    @DisplayName("게시글 작성 실패 - 인가되지 않음(비멤버)")
    void writePostFail_Unauthenticated() {
        // given
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(any(), anyString()))
                .thenReturn(null);

        // when & then
        Assertions.assertThrows(UnauthenticatedException.class,
                () -> blogPostService.createPost("caboom", 1L, request));
    }

    @Test
    @DisplayName("게시글 작성 실패 - 인가되지 않음(추방된 멤버)")
    void writePostFail_Unauthenticated_case2() {
        // given
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(any(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(
                        testBlog,
                        testMember,
                        Role.ofNewRole("ROLE_KICKED", "추방된_회원", ""),
                        "새연"
                ));

        // when & then
        Assertions.assertThrows(UnauthenticatedException.class,
                () -> blogPostService.createPost("caboom", 1L, request));
    }

    @Test
    @DisplayName("게시글 작성 실패 - 카테고리를 선택하지 않았지만 DB에 기본 카테고리 없음")
    void writePostFail_DatabaseException() throws Exception {
        // given
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(any(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(
                        testBlog, testMember, roleOwner, "소유자"
                ));
        categoryIdsField.set(request, List.of());
        Mockito.when(categoryRepository.findByBlog_BlogFidAndAndCategoryName(anyString(), anyString()))
                .thenReturn(Optional.empty());

        // when & then
        Assertions.assertThrows(DatabaseException.class,
                () -> blogPostService.createPost("caboom", 1L, request));
    }

    @Test
    @DisplayName("게시글 작성 실패 - 카테고리가 다른 블로그 소속")
    void writePostFail_OtherBlogCategory() {
        // given
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(any(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(
                        testBlog, testMember, roleOwner, "소유자"
                ));
        Blog otherBlog = Blog.ofExistingBlog(2L, "otherBlog", true, "다른블로그",
                "", true, null, BlogType.PERSONAL);
        Category otherBlogCategory = Category.ofNewCategory(otherBlog, null, testTopic, "테스트카테고리",
                true, 1, 1);
        Mockito.when(categoryRepository.findByCategoryId(anyLong()))
                .thenReturn(Optional.of(otherBlogCategory));

        // when & then
        Assertions.assertThrows(BadRequestException.class,
                () -> blogPostService.createPost("caboom", 1L, request));
    }

    @Test
    @DisplayName("게시글 작성 실패 - 비공개 블로그")
    void writePostFail_PrivateBlogPublicPost() {
        // given
        Blog privateBlog = Blog.ofExistingBlog(2L, "privateBlog", true, "비공개",
                "", false, null, BlogType.PERSONAL);
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(any(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(
                        privateBlog, testMember, roleOwner, "소유자"
                ));
        Mockito.when(categoryRepository.findByCategoryId(anyLong()))
                .thenReturn(Optional.of(testPublicCategory));

        // when & then
        Assertions.assertThrows(BadRequestException.class,
                () -> blogPostService.createPost("caboom", 1L, request));
    }

    @Test
    @DisplayName("게시글 작성 실패 - 비공개 카테고리")
    void writePostFail_PrivateCategoryPublicPost() {
        // given
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(any(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(
                        testBlog, testMember, roleOwner, "소유자"
                ));
        Mockito.when(categoryRepository.findByCategoryId(anyLong()))
                .thenReturn(Optional.of(testPrivateCategory));

        // when & then
        Assertions.assertThrows(BadRequestException.class,
                () -> blogPostService.createPost("caboom", 1L, request));
    }

    @Test
    @DisplayName("게시글 작성 성공 - 카테고리 선택 안하면 기본 카테고리로 등록됨")
    void writePostSuccess_NoCategorySelect() throws Exception {
        // given
        categoryIdsField.set(request, List.of());
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(any(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(
                        testBlog, testMember, roleOwner, "소유자"
                ));
        Category noneCategory = Category.ofNewCategory(testBlog, null, null, "카테고리 없음",
                true, 0, 0);
        Field categoryIdField = Category.class.getDeclaredField("categoryId");
        categoryIdField.setAccessible(true);
        categoryIdField.set(noneCategory, 1L);
        Mockito.when(categoryRepository.findByBlog_BlogFidAndAndCategoryName(anyString(), anyString()))
                .thenReturn(Optional.of(noneCategory));
        Mockito.when(categoryRepository.findByCategoryId(anyLong()))
                .thenReturn(Optional.of(noneCategory));

        // when
        blogPostService.createPost("caboom", 1L, request);
        Mockito.verify(postCategoryMappingRepository).save(postCategoryMappingArgumentCaptor.capture());
        PostCategoryMapping savedMapping = postCategoryMappingArgumentCaptor.getValue();

        // then
        Assertions.assertEquals("카테고리 없음", savedMapping.getCategory().getCategoryName());
        Mockito.verify(postRepository, Mockito.times(1)).save(any());
    }

    @Test
    @DisplayName("게시글 작성 성공 - 카테고리 선택")
    void writePostSuccess() throws Exception {
        // given
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(any(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(
                        testBlog, testMember, roleOwner, "소유자"
                ));
        Mockito.when(categoryRepository.findByCategoryId(anyLong()))
                .thenReturn(Optional.of(testPublicCategory));

        // when
        blogPostService.createPost("caboom", 1L, request);

        // then
        Mockito.verify(postCategoryMappingRepository, Mockito.times(request.getCategoryIds().size())).save(any());
        Mockito.verify(postRepository, Mockito.times(1)).save(any());
    }

    @Test
    @DisplayName("게시글 단일조회 실패 - 존재하지 않는 게시글")
    void getPostDetailFail() {
        // given
        Mockito.when(postRepository.findByPostId(anyLong())).thenReturn(Optional.empty());

        // when & then
        Assertions.assertThrows(PostNotFoundException.class,
                () -> blogPostService.getPostDetail("caboom", 1L, 1L));
    }

    @Test
    @DisplayName("비공개 게시글 단일조회 실패 - 로그인하지 않았음")
    void getPostDetailFail_Unauthorized() {
        // given
        Post post = Post.ofNewPost(testBlog, testMember, "제목", "안녕하세요",
                false, null);
        Mockito.when(postRepository.findByPostId(anyLong()))
                .thenReturn(Optional.of(post));

        // when & then
        Assertions.assertThrows(UnauthenticatedException.class,
                () -> blogPostService.getPostDetail("caboom", 1L, null));
    }

    @Test
    @DisplayName("비공개 게시글 단일조회 성공")
    void getPostDetailSuccess_Public() {
        // given
        Post post = Post.ofNewPost(testBlog, testMember, "제목", "안녕하세요",
                false, null);
        Mockito.when(postRepository.findByPostId(anyLong()))
                .thenReturn(Optional.of(post));
        Mockito.when(postRepositoryCustom.findPostDetailById(anyLong()))
                .thenReturn(Optional.of(new PostDetailResponse(1L,
                        new TeamBlogMemberResponse(UUID.randomUUID().toString(), "작성자", "caboom"),
                        null, "제목", "내용",
                        false, 1L, null, null,
                        List.of("카테고리1"))));


        // when
        PostDetailResponse response = blogPostService.getPostDetail("caboom", 1L, testMember.getMbNo());

        // then
        Assertions.assertEquals("제목", response.getTitle());
    }

    @Test
    @DisplayName("비공개 게시글 단일조회 실패 - 블로그 소유자 또는 멤버가 아님")
    void getPostDetailFail_Unauthenticated() {
        // given
        Post post = Post.ofNewPost(testBlog, testMember, "제목", "안녕하세요",
                false, null);
        Mockito.when(postRepository.findByPostId(anyLong()))
                .thenReturn(Optional.of(post));

        // when & then
        Assertions.assertThrows(UnauthenticatedException.class,
                () -> blogPostService.getPostDetail("caboom", 1L, 200L));
    }

    @Test
    @DisplayName("게시글 단일조회 실패 - 게시글이 블로그 소속이 아님")
    void getPostDetailFail_BadRequest() {
        // given
        Post post = Post.ofNewPost(testBlog, testMember, "제목", "안녕하세요",
                false, null);
        Mockito.when(postRepository.findByPostId(anyLong()))
                .thenReturn(Optional.of(post));

        // when & then
        Assertions.assertThrows(BadRequestException.class,
                () -> blogPostService.getPostDetail("otherBlog", 1L, testMember.getMbNo()));
    }

    @Test
    @DisplayName("공개 게시글 단일조회 성공 - 로그인하지 않았을때")
    void getPostDetailSuccess_Private() {
        // given
        Post post = Post.ofNewPost(testBlog, testMember, "제목", "안녕하세요",
                true, null);
        Mockito.when(postRepository.findByPostId(anyLong()))
                .thenReturn(Optional.of(post));
        Mockito.when(postRepositoryCustom.findPostDetailById(anyLong()))
                .thenReturn(Optional.of(new PostDetailResponse(1L,
                        new TeamBlogMemberResponse(UUID.randomUUID().toString(), "작성자", "caboom"),
                        null, "제목", "내용",
                        true, 1L, null, null,
                        List.of("카테고리1"))));


        // when
        PostDetailResponse response = blogPostService.getPostDetail("caboom", 1L, null);

        // then
        Assertions.assertEquals("내용", response.getContent());
    }

    @Test
    @DisplayName("공개 게시글 단일조회 성공 - 로그인했지만 블로그 비회원일때")
    void getPostDetailSuccess_Private_NonMember() {
        // given
        Post post = Post.ofNewPost(testBlog, testMember, "제목", "안녕하세요",
                true, null);
        Mockito.when(postRepository.findByPostId(anyLong()))
                .thenReturn(Optional.of(post));
        Mockito.when(postRepositoryCustom.findPostDetailById(anyLong()))
                .thenReturn(Optional.of(new PostDetailResponse(1L,
                        new TeamBlogMemberResponse(UUID.randomUUID().toString(), "작성자", "caboom"),
                        null, "제목", "내용",
                        true, 1L, null, null,
                        List.of("카테고리1"))));

        // when
        PostDetailResponse response = blogPostService.getPostDetail("caboom", 100L, null);

        // then
        Assertions.assertEquals("내용", response.getContent());
    }

}