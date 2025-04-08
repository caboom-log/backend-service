package site.caboomlog.backendservice.blog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import site.caboomlog.backendservice.blog.dto.BlogInfoResponse;
import site.caboomlog.backendservice.blog.dto.CreateBlogRequest;
import site.caboomlog.backendservice.blog.dto.ModifyBlogInfoRequest;
import site.caboomlog.backendservice.blog.entity.Blog;
import site.caboomlog.backendservice.blog.entity.BlogType;
import site.caboomlog.backendservice.blog.exception.InvalidBlogCountRangeException;
import site.caboomlog.backendservice.blog.repository.BlogRepository;
import site.caboomlog.backendservice.blogmember.entity.BlogMemberMapping;
import site.caboomlog.backendservice.blogmember.repository.BlogMemberMappingRepository;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.common.exception.UnauthenticatedException;
import site.caboomlog.backendservice.member.entity.Member;
import site.caboomlog.backendservice.member.exception.MemberNotFoundException;
import site.caboomlog.backendservice.member.repository.MemberRepository;
import site.caboomlog.backendservice.role.entity.Role;
import site.caboomlog.backendservice.role.repository.RoleRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class BlogServiceTest {

    @Mock
    BlogRepository blogRepository;
    @Mock
    BlogMemberMappingRepository blogMemberMappingRepository;
    @Mock
    MemberRepository memberRepository;
    @Mock
    RoleRepository roleRepository;
    @InjectMocks
    BlogService blogService;

    ObjectMapper objectMapper = new ObjectMapper();

    Member testMember = Member.ofExistingMember(1L, "test@test.com", "세연",
            "1234qwer", "010-1234-5678", null, null);
    Role roleOwner = Role.ofNewRole("ROLE_OWNER", "블로그 소유자", "블로그 소유자 입니다.");
    Blog testBlog = Blog.ofExistingBlog(1L, "caboom", true,
            "카붐로그", "안녕하세요", true,
            null, BlogType.valueOf("PERSONAL"));

    @Test
    @DisplayName("블로그 정보 조회 성공")
    void getBlogInfoSuccess() {
        // given
        Blog testBlog = Blog
                .ofExistingBlog(1L, "caboom", true,
                        "카붐로그", "안녕하세요", true,
                        null, BlogType.valueOf("PERSONAL"));
        Mockito.when(blogRepository.findByBlogFid(anyString()))
                .thenReturn(Optional.of(testBlog));

        // when
        BlogInfoResponse response = blogService.getBlogInfo("caboom");

        // then
        Assertions.assertEquals(response.getBlogName(), testBlog.getBlogName());
    }

    @Test
    @DisplayName("블로그 생성 실패 - 유효하지 않은 mbNo")
    void createBlogFail_MemberNotFound() {
        // given
        CreateBlogRequest request = new CreateBlogRequest("testFid",
                "testNickname",
                "test",
                "안녕하세요",
                true,
                "personal");
        Mockito.when(memberRepository.findById(anyLong()))
                .thenThrow(new MemberNotFoundException("존재하지 않는 사용자입니다."));

        // when & then
        Assertions.assertThrows(MemberNotFoundException.class,
                () -> blogService.createBlog(request, 1L));
    }

    @Test
    @DisplayName("블로그 생성 성공 - 첫 블로그일 경우 메인 블로그로 설정")
    void createBlogSuccess_FirstBlogSetsAsMain() {
        // given
        CreateBlogRequest request = new CreateBlogRequest(
                "caboom", "세연", "카붐로그", "안녕하세요", true, "personal");

        Mockito.when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testMember));
        Mockito.when(roleRepository.findById("ROLE_OWNER")).thenReturn(Optional.of(roleOwner));
        Mockito.when(blogRepository.existsByBlogFid(anyString())).thenReturn(false);
        Mockito.when(blogMemberMappingRepository.countByMember_MbNo(anyLong())).thenReturn(0);
        Mockito.when(blogMemberMappingRepository.existsByMember_MbNoAndBlogBlogMain(anyLong(), eq(true)))
                .thenReturn(false);

        Mockito.when(blogRepository.save(Mockito.any(Blog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        blogService.createBlog(request, 1L);

        // then
        Mockito.verify(blogRepository).save(Mockito.argThat(blog -> blog.getBlogMain().equals(true)));
    }

    @Test
    @DisplayName("블로그 생성 성공 - 첫 블로그가 아닐 경우 메인 블로그 x")
    void createBlogSuccess() {
        // given
        CreateBlogRequest request = new CreateBlogRequest(
                "caboom", "세연", "카붐로그", "안녕하세요", true, "personal");

        Mockito.when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testMember));
        Mockito.when(roleRepository.findById("ROLE_OWNER")).thenReturn(Optional.of(roleOwner));
        Mockito.when(blogRepository.existsByBlogFid(anyString())).thenReturn(false);
        Mockito.when(blogMemberMappingRepository.countByMember_MbNo(anyLong())).thenReturn(0);
        Mockito.when(blogMemberMappingRepository.existsByMember_MbNoAndBlogBlogMain(anyLong(), eq(true)))
                .thenReturn(true);

        Mockito.when(blogRepository.save(Mockito.any(Blog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        blogService.createBlog(request, 1L);

        // then
        Mockito.verify(blogRepository).save(Mockito.argThat(blog -> blog.getBlogMain().equals(false)));
    }

    @Test
    @DisplayName("블로그 생성 실패 - 블로그 갯수 초과")
    void createBlogFail_BlogCountIsMoreThanThree() {
        // given
        CreateBlogRequest request = new CreateBlogRequest(
                "caboom", "세연", "카붐로그", "안녕하세요", true, "personal");

        Mockito.when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testMember));
        Mockito.when(roleRepository.findById("ROLE_OWNER")).thenReturn(Optional.of(roleOwner));
        Mockito.when(blogRepository.existsByBlogFid(anyString())).thenReturn(false);
        Mockito.when(blogMemberMappingRepository.countByMember_MbNo(anyLong())).thenReturn(3);

        // when & then
        Assertions.assertThrows(InvalidBlogCountRangeException.class,
                () -> blogService.createBlog(request, 1L));
    }

    @Test
    @DisplayName("블로그 정보 수정 실패 - 블로그 소유자가 아님")
    void modifyBlogInfoFail_BlogNotExists() throws Exception {
        // given
        String requestStr = """
            {
                "blogName": "바꾼이름",
                "blogDesc": "반갑습니다.",
                "blogPublic": true
            }
        """;
        ModifyBlogInfoRequest request = objectMapper.readValue(requestStr, ModifyBlogInfoRequest.class);

        BlogMemberMapping mapping = BlogMemberMapping.ofNewBlogMemberMapping(
                Blog.ofExistingBlog(1L, "caboom", true,
                        "카붐로그", "안녕하세요", true,
                        null, BlogType.valueOf("PERSONAL")),
                testMember,
                Role.ofNewRole("ROLE_MEMBER", "블로그_멤버", "세연"),
                "임새"
        );
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(mapping);

        // when & then
        Assertions.assertThrows(UnauthenticatedException.class,
                () -> blogService.modifyBlogInfo("caboom", 1L, request));
    }

    @Test
    @DisplayName("블로그 정보 수정 성공")
    void modifyBlogInfoSuccess() throws Exception {
        // given
        String requestStr = """
            {
                "blogName": "바꾼이름",
                "blogDesc": "반갑습니다.",
                "blogPublic": true
            }
        """;
        ModifyBlogInfoRequest request = objectMapper.readValue(requestStr, ModifyBlogInfoRequest.class);
        BlogMemberMapping mapping = BlogMemberMapping.ofNewBlogMemberMapping(
                testBlog,
                testMember,
                roleOwner,
                "임새"
        );
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(mapping);
        Mockito.when(blogRepository.findByBlogFid(anyString()))
                .thenReturn(Optional.of(testBlog));

        // when & then
        Assertions.assertDoesNotThrow(() -> blogService.modifyBlogInfo("caboom", 1L, request));
    }

    @Test
    @DisplayName("메인 블로그 변경 실패 - 블로그 소유자가 아님")
    void switchMainBlogFail_Unauthenticated () {
        // given
        BlogMemberMapping mapping = BlogMemberMapping.ofNewBlogMemberMapping(
                testBlog,
                testMember,
                Role.ofNewRole("ROLE_MEMBER", "블로그_멤버", "세연"),
                "임새"
        );
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(mapping);

        // when & then
        Assertions.assertThrows(UnauthenticatedException.class,
                () -> blogService.switchMainBlogTo("caboom", 1L));
    }

    @Test
    @DisplayName("메인 블로그 변경 실패 - 기존 메인블로그가 없음")
    void switchMainBlogFail_MainBlogNotExist () {
        // given
        BlogMemberMapping mapping = BlogMemberMapping.ofNewBlogMemberMapping(
                testBlog,
                testMember,
                roleOwner,
                "임새"
        );
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(mapping);
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogMain(anyLong(), anyBoolean()))
                .thenReturn(null);

        // when & then
        Assertions.assertThrows(BadRequestException.class,
                () -> blogService.switchMainBlogTo("caboom", 1L));
    }

    @Test
    @DisplayName("메인 블로그 변경 성공")
    void switchMainBlogSuccess () {
        // given
        BlogMemberMapping mapping = BlogMemberMapping.ofNewBlogMemberMapping(
                testBlog,
                testMember,
                roleOwner,
                "임새"
        );
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(mapping);
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogMain(anyLong(), anyBoolean()))
                .thenReturn(mapping);

        // when & then
        Assertions.assertDoesNotThrow(() -> blogService.switchMainBlogTo("caboom", 1L));
    }
}