package site.caboomlog.backendservice.blog.service;

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
import site.caboomlog.backendservice.blog.entity.TeamBlogInvite;
import site.caboomlog.backendservice.blog.entity.TeamBlogInviteStatus;
import site.caboomlog.backendservice.blog.exception.BlogMemberNicknameConflictException;
import site.caboomlog.backendservice.blog.repository.BlogRepository;
import site.caboomlog.backendservice.blog.repository.TeamBlogInviteRepository;
import site.caboomlog.backendservice.blogmember.entity.BlogMemberMapping;
import site.caboomlog.backendservice.blogmember.repository.BlogMemberMappingRepository;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.member.entity.Member;
import site.caboomlog.backendservice.role.entity.Role;
import site.caboomlog.backendservice.role.repository.RoleRepository;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class TeamBlogServiceTest {
    @Mock
    BlogRepository blogRepository;
    @Mock
    TeamBlogInviteRepository teamBlogInviteRepository;
    @Mock
    BlogMemberMappingRepository blogMemberMappingRepository;
    @Mock
    RoleRepository roleRepository;

    @InjectMocks
    TeamBlogService teamBlogService;

    Blog testBlog = Blog.ofExistingBlog(1L, "caboom", true, "카붐로그",
            "안녕하세요.", true, null, BlogType.TEAM);
    Member testMember = Member.ofExistingMember(2L, "test@test.com", "test", "1234qwer",
            "010-1111-2222", null, null);
    Role roleMember = Role.ofNewRole("ROLE_MEMBER", "블로그_멤버", "블로그 멤버 입니다.");

    @Test
    @DisplayName("팀 블로그 초대 수락 실패 - 초대 받은 적이 없음")
    void joinTeamBlogFail_NoInvitations() {
        // given
        Mockito.when(blogRepository.findByBlogFid(anyString()))
                .thenReturn(Optional.of(testBlog));
        Mockito.when(teamBlogInviteRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(null);

        // when & then
        Assertions.assertThrows(BadRequestException.class,
                () -> teamBlogService.joinTeamBlog("caboom", 2L, "초대받고싶은멤버"));
    }

    @Test
    @DisplayName("팀 블로그 초대 수락 실패 - 초대 상태가 PENDING이 아님")
    void joinTeamBlogFail_NoPending() throws Exception {
        // given
        Mockito.when(blogRepository.findByBlogFid(anyString()))
                .thenReturn(Optional.of(testBlog));
        TeamBlogInvite invite = TeamBlogInvite.ofNewTeamBlogInvite(testBlog, testMember);
        Mockito.when(teamBlogInviteRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(invite);

        Field statusField = TeamBlogInvite.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(invite, TeamBlogInviteStatus.REJECTED);

        // when & then
        Assertions.assertThrows(BadRequestException.class,
                () -> teamBlogService.joinTeamBlog("caboom", 2L, "초대받고싶은멤버"));
    }

    @Test
    @DisplayName("팀 블로그 초대 수락 실패 - 이미 가입된 멤버")
    void joinTeamBlogFail_AlreadyMember() throws Exception {
        // given
        Mockito.when(blogRepository.findByBlogFid(anyString()))
                .thenReturn(Optional.of(testBlog));
        TeamBlogInvite invite = TeamBlogInvite.ofNewTeamBlogInvite(testBlog, testMember);
        Mockito.when(teamBlogInviteRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(invite);
        Mockito.when(teamBlogInviteRepository.save(any()))
                .thenReturn(invite);
        Mockito.when(roleRepository.findByRoleId(anyString())).thenReturn(roleMember);
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(BlogMemberMapping.ofNewBlogMemberMapping(
                        testBlog, testMember, roleMember, "이미 가입된 멤버"
                ));

        // when & then
        Assertions.assertThrows(BadRequestException.class,
                () -> teamBlogService.joinTeamBlog("caboom", 2L, "초대받고싶은멤버"));
    }

    @Test
    @DisplayName("팀 블로그 초대 수락 실패 - 닉네임 중복")
    void joinTeamBlogFail_MbNicknameConflict() {
        // given
        Mockito.when(blogRepository.findByBlogFid(anyString()))
                .thenReturn(Optional.of(testBlog));
        TeamBlogInvite invite = TeamBlogInvite.ofNewTeamBlogInvite(testBlog, testMember);
        Mockito.when(teamBlogInviteRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(invite);
        Mockito.when(teamBlogInviteRepository.save(any()))
                .thenReturn(invite);
        Mockito.when(roleRepository.findByRoleId(anyString())).thenReturn(roleMember);
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(null);
        Mockito.when(blogMemberMappingRepository.existsByBlog_BlogFidAndMbNickname(anyString(), anyString()))
                .thenReturn(true);

        // when & then
        Assertions.assertThrows(BlogMemberNicknameConflictException.class,
                () -> teamBlogService.joinTeamBlog("caboom", 2L, "초대받고싶은멤버"));
    }

    @Test
    @DisplayName("팀 블로그 초대 수락 성공")
    void joinTeamBlogSuccess() {
        // given
        Mockito.when(blogRepository.findByBlogFid(anyString()))
                .thenReturn(Optional.of(testBlog));
        TeamBlogInvite invite = TeamBlogInvite.ofNewTeamBlogInvite(testBlog, testMember);
        Mockito.when(teamBlogInviteRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(invite);
        Mockito.when(teamBlogInviteRepository.save(any()))
                .thenReturn(invite);
        Mockito.when(roleRepository.findByRoleId(anyString())).thenReturn(roleMember);
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(null);
        Mockito.when(blogMemberMappingRepository.existsByBlog_BlogFidAndMbNickname(anyString(), anyString()))
                .thenReturn(false);

        // when
        teamBlogService.joinTeamBlog("caboom", 2L, "초대받고싶은멤버");

        // then
        Assertions.assertEquals(TeamBlogInviteStatus.ACCEPTED, invite.getStatus());

        Mockito.verify(teamBlogInviteRepository, Mockito.times(1))
                .save(any());
        Mockito.verify(blogMemberMappingRepository, Mockito.times(1))
                .save(any());
    }
}