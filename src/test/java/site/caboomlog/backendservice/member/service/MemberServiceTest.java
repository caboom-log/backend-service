package site.caboomlog.backendservice.member.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import site.caboomlog.backendservice.blog.entity.Blog;
import site.caboomlog.backendservice.blog.entity.BlogType;
import site.caboomlog.backendservice.blogmember.entity.BlogMemberMapping;
import site.caboomlog.backendservice.blogmember.repository.BlogMemberMappingRepository;
import site.caboomlog.backendservice.member.dto.GetMemberResponse;
import site.caboomlog.backendservice.member.entity.Member;
import site.caboomlog.backendservice.role.entity.Role;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock
    BlogMemberMappingRepository blogMemberMappingRepository;

    @InjectMocks
    MemberService memberService;

    Blog testBlog = Blog.ofExistingBlog(
            1L, "caboom", true, "카붐로그",
            "안녕하세요", true, null, BlogType.PERSONAL
    );
    Role roleOwner = Role.ofNewRole("ROLE_OWNER", "블로그_소유자", "블로그 소유자 입니다.");
    Member testMember = Member.ofExistingMember(
            1L, "caboom@test.com", "카붐", "1234qwer",
            "010-0000-1111", null, null
    );

    @Test
    @DisplayName("멤버 정보 조회 성공")
    void getMemberByMbNo() {
        // given
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogMain(anyLong(), anyBoolean()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(
                        testBlog, testMember, roleOwner, "소유자"
                ));

        // when
        GetMemberResponse response = memberService.getMemberByMbNo(1L);

        // then
        Assertions.assertEquals(response.getMbEmail(), testMember.getMbEmail());
        Assertions.assertEquals(response.getMainBlogFid(), testBlog.getBlogFid());
    }
}