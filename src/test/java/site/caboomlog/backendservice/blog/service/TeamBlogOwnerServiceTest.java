package site.caboomlog.backendservice.blog.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import site.caboomlog.backendservice.blog.entity.Blog;
import site.caboomlog.backendservice.blog.entity.BlogType;
import site.caboomlog.backendservice.blog.exception.AlreadyInvitedException;
import site.caboomlog.backendservice.blog.repository.TeamBlogInviteRepository;
import site.caboomlog.backendservice.blogmember.BlogMemberMapping;
import site.caboomlog.backendservice.blogmember.repository.BlogMemberMappingRepository;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.common.exception.DatabaseException;
import site.caboomlog.backendservice.common.exception.UnauthenticatedException;
import site.caboomlog.backendservice.common.notification.entity.NotificationType;
import site.caboomlog.backendservice.common.notification.event.NotificationCreatedEvent;
import site.caboomlog.backendservice.common.notification.repository.NotificationRepository;
import site.caboomlog.backendservice.common.notification.repository.NotificationTypeRepository;
import site.caboomlog.backendservice.member.entity.Member;
import site.caboomlog.backendservice.member.exception.MemberNotFoundException;
import site.caboomlog.backendservice.member.exception.MemberWithdrawException;
import site.caboomlog.backendservice.member.repository.MemberRepository;
import site.caboomlog.backendservice.role.entity.Role;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class TeamBlogOwnerServiceTest {
    @Mock
    BlogMemberMappingRepository blogMemberMappingRepository;
    @Mock
    NotificationRepository notificationRepository;
    @Mock
    NotificationTypeRepository notificationTypeRepository;
    @Mock
    MemberRepository memberRepository;
    @Mock
    TeamBlogInviteRepository teamBlogInviteRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;
    @Mock
    TeamBlogMemberKicker teamBlogMemberKicker;

    @InjectMocks
    TeamBlogOwnerService teamBlogOwnerService;

    Blog testBlog = Blog.ofExistingBlog(
            1L, "caboom", true, "카붐로그",
            "안녕하세요", true, null, BlogType.TEAM
    );
    Member testOwner = Member.ofExistingMember(
            1L, "caboom@test.com", "caboom", "1234qwer",
            "010-1111-2222", null, null
    );
    Role roleOwner = Role.ofNewRole("ROLE_OWNER", "블로그 소유자", "블로그 소유자 입니다");
    Role roleMember = Role.ofNewRole("ROLE_MEMBER", "블로그 멤버", "블로그 멤버 입니다.");
    Member testMember = Member.ofExistingMember(
            2L, "test@test.com", "회원1", "1234qwer",
            "010-0000-0000", null, null
    );
    NotificationType notificationType = new NotificationType();

    @Test
    @DisplayName("팀 블로그에 멤버 초대 실패 - 소유자 권한 없음")
    void inviteMemberFail_Unauthenticated() {
        // given
        BlogMemberMapping mapping = BlogMemberMapping.ofNewBlogMemberMapping(
                testBlog, testMember, roleMember, "하잉"
        );
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(mapping);

        // when & then
        Assertions.assertThrows(UnauthenticatedException.class,
                () -> teamBlogOwnerService.inviteMember(testOwner.getMbNo(),
                        testMember.getMbUuid(),
                        testBlog.getBlogFid()));
    }

    @Test
    @DisplayName("팀 블로그에 멤버 초대 실패 - 팀 블로그 아님")
    void inviteMemberFail_NotTeamBlog() {
        // given
        Blog personalBlog = Blog.ofExistingBlog(
                1L, "caboom", true, "카붐로그",
                "안녕하세요", true, null, BlogType.PERSONAL
        );
        BlogMemberMapping mapping = BlogMemberMapping.ofNewBlogMemberMapping(
                personalBlog, testMember, roleOwner, "하잉"
        );
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(mapping);

        // when & then
        Assertions.assertThrows(BadRequestException.class,
                () -> teamBlogOwnerService.inviteMember(testOwner.getMbNo(),
                        testMember.getMbUuid(),
                        testBlog.getBlogFid()));
    }

    @Test
    @DisplayName("팀 블로그에 멤버 초대 실패 - 탈퇴한 회원")
    void inviteMemberFail_Withdraw() throws Exception {
        // given
        Member member = Member.ofExistingMember(
                1L, "test@test.com", "caboom", "1234qwer",
                "010-0000-0000", null, null
        );
        Field withdrawalAtField = Member.class.getDeclaredField("withdrawalAt");
        withdrawalAtField.setAccessible(true);
        withdrawalAtField.set(member, LocalDateTime.now());
        BlogMemberMapping mapping = BlogMemberMapping.ofNewBlogMemberMapping(
                testBlog, testOwner, roleOwner, "블로그주인장"
        );

        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(mapping);
        Mockito.when(memberRepository.findByMbUuid(anyString()))
                .thenReturn(Optional.of(member));

        // when & then
        Assertions.assertThrows(MemberWithdrawException.class,
            () -> teamBlogOwnerService.inviteMember(testOwner.getMbNo(), member.getMbUuid(), testBlog.getBlogFid()));
    }

    @Test
    @DisplayName("팀 블로그에 멤버 초대 실패 - 이미 초대한 회원")
    void inviteMemberFail_AlreadyInvited() {
        // given
        BlogMemberMapping mapping = BlogMemberMapping.ofNewBlogMemberMapping(
                testBlog, testOwner, roleOwner, "블로그주인장"
        );

        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(mapping);
        Mockito.when(memberRepository.findByMbUuid(anyString()))
                .thenReturn(Optional.of(testMember));
        Mockito.when(teamBlogInviteRepository.existsByMember_MbNoAndBlog_BlogFidAndAndStatus(anyLong(), anyString(), any()))
                .thenReturn(true);

        // when & then
        Assertions.assertThrows(AlreadyInvitedException.class,
                () -> teamBlogOwnerService.inviteMember(testOwner.getMbNo(), testMember.getMbUuid(), testBlog.getBlogFid()));
    }

    @Test
    @DisplayName("팀 블로그에 멤버 초대 실패 - 데이터베이스에 NotificationType.INVITE 저장 x")
    void inviteMemberFail_DatabaseException() {
        // given
        BlogMemberMapping mapping = BlogMemberMapping.ofNewBlogMemberMapping(
                testBlog, testOwner, roleOwner, "블로그주인장"
        );

        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(mapping);
        Mockito.when(memberRepository.findByMbUuid(anyString()))
                .thenReturn(Optional.of(testMember));
        Mockito.when(teamBlogInviteRepository.existsByMember_MbNoAndBlog_BlogFidAndAndStatus(anyLong(), anyString(), any()))
                .thenReturn(false);
        Mockito.when(notificationTypeRepository.findByNotificationTypeName(anyString()))
                .thenReturn(null);

        // when & then
        Assertions.assertThrows(DatabaseException.class,
                () -> teamBlogOwnerService.inviteMember(testOwner.getMbNo(), testMember.getMbUuid(), testBlog.getBlogFid()));
    }

    @Test
    @DisplayName("팀 블로그에 멤버 초대 성공")
    void inviteMemberSuccess() {
        // given
        BlogMemberMapping mapping = BlogMemberMapping.ofNewBlogMemberMapping(
                testBlog, testOwner, roleOwner, "블로그주인장"
        );

        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(mapping);
        Mockito.when(memberRepository.findByMbUuid(anyString()))
                .thenReturn(Optional.of(testMember));
        Mockito.when(teamBlogInviteRepository.existsByMember_MbNoAndBlog_BlogFidAndAndStatus(anyLong(), anyString(), any()))
                .thenReturn(false);
        Mockito.when(notificationTypeRepository.findByNotificationTypeName(anyString()))
                .thenReturn(notificationType);
        Mockito.when(memberRepository.findById(anyLong()))
                .thenReturn(Optional.of(testMember));

        // when
        teamBlogOwnerService.inviteMember(testOwner.getMbNo(), testMember.getMbUuid(), testBlog.getBlogFid());

        // then
        Mockito.verify(teamBlogInviteRepository, Mockito.times(1))
                .save(any());
        Mockito.verify(notificationRepository, Mockito.times(1))
                .save(any());
        Mockito.verify(eventPublisher, Mockito.times(1))
                .publishEvent(Mockito.any(NotificationCreatedEvent.class));
    }

    @Test
    @DisplayName("팀 블로그 멤버 추방 실패 - 소유자가 아님")
    void kickMemberFail_Unauthenticated() {
        // given
        BlogMemberMapping mapping = BlogMemberMapping.ofNewBlogMemberMapping(
                testBlog, testOwner, roleMember, "블로그멤버"
        );
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(mapping);

        // when & then
        Assertions.assertThrows(UnauthenticatedException.class,
                () -> teamBlogOwnerService.kickMember("caboom", 1L, "세연"));
    }

    @Test
    @DisplayName("팀 블로그 멤버 추방 실패 - 팀 블로그가 아님")
    void kickMemberFail_NotTeamBlog() {
        // given
        Blog personalBlog = Blog.ofExistingBlog(
                1L, "caboom", true, "카붐로그",
                "안녕하세요", true, null, BlogType.PERSONAL
        );
        BlogMemberMapping mapping = BlogMemberMapping.ofNewBlogMemberMapping(
                personalBlog, testOwner, roleOwner, "블로그주인"
        );
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(mapping);

        // when & then
        Assertions.assertThrows(BadRequestException.class,
                () -> teamBlogOwnerService.kickMember("caboom", 1L, "세연"));
    }

    @Test
    @DisplayName("팀 블로그 멤버 추방 실패 - 추방 대상 멤버가 팀 블로그 멤버가 아님(mbNcknameNotFound)")
    void kickMemberFail_NotTeamBlogMember() {
        // given
        BlogMemberMapping mapping = BlogMemberMapping.ofNewBlogMemberMapping(
                testBlog, testOwner, roleOwner, "블로그주인"
        );
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(mapping);
        Mockito.when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testOwner));
        Mockito.when(blogMemberMappingRepository.findByBlog_BlogFidAndMbNickname(anyString(), anyString()))
                .thenReturn(null);

        // when & then
        Assertions.assertThrows(MemberNotFoundException.class,
                () -> teamBlogOwnerService.kickMember("caboom", 1L, "세연"));
    }

    @Test
    @DisplayName("팀 블로그 멤버 추방 성공")
    void kickMemberSuccess() {
        // given
        BlogMemberMapping mapping = BlogMemberMapping.ofNewBlogMemberMapping(
                testBlog, testOwner, roleOwner, "블로그주인"
        );
        BlogMemberMapping targetMapping = BlogMemberMapping.ofNewBlogMemberMapping(
                testBlog, testMember, roleMember, "추방멤버"
        );
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(mapping);
        Mockito.when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testOwner));
        Mockito.when(blogMemberMappingRepository.findByBlog_BlogFidAndMbNickname(anyString(), anyString()))
                .thenReturn(targetMapping);
        Mockito.doNothing().when(teamBlogMemberKicker).kickSingleMember(any(), any(), any());

        // when
        teamBlogOwnerService.kickMember("caboom", 1L, "세연");

        // then
        Mockito.verify(teamBlogMemberKicker, Mockito.times(1))
                .kickSingleMember(any(), any(), any());
    }

    @Test
    @DisplayName("팀 블로그 멤버 전체 추방")
    void kickAllMembersSuccess() {
        // given
        BlogMemberMapping mapping = BlogMemberMapping.ofNewBlogMemberMapping(
                testBlog, testOwner, roleOwner, "블로그주인"
        );
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(mapping);
        Mockito.when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testOwner));

        List<BlogMemberMapping> mappings = List.of(
                BlogMemberMapping.ofNewBlogMemberMapping(testBlog, testMember, roleMember, "멤버1"),
                BlogMemberMapping.ofNewBlogMemberMapping(testBlog, testMember, roleMember, "멤버2")
        );
        Mockito.when(blogMemberMappingRepository.findAllByBlog_BlogFidAndRole_RoleId(anyString(), anyString()))
                        .thenReturn(mappings);
        Mockito.doNothing().when(teamBlogMemberKicker).kickSingleMember(any(), any(), any());

        // when
        teamBlogOwnerService.kickAllMembers("caboom", 1L);

        // then
        Mockito.verify(teamBlogMemberKicker, Mockito.times(2))
                .kickSingleMember(any(), any(), any());
    }

    @Test
    @DisplayName("블로그 소유자 위임 실패 - 소유자가 아님")
    void transferOwnershipFail_Unauthenticated() {
        // given
        BlogMemberMapping mappingNotOwner = BlogMemberMapping.ofNewBlogMemberMapping(
                testBlog, testMember, roleMember, "소유자인 척하는 멤버"
        );
        BlogMemberMapping newOwner = BlogMemberMapping.ofNewBlogMemberMapping(
                testBlog, testMember, roleMember, "새로운 소유자가 될 멤버"
        );
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(mappingNotOwner);
        Mockito.when(blogMemberMappingRepository.findByMember_MbUuidAndBlog_BlogFid(anyString(), anyString()))
                .thenReturn(newOwner);

        // when & then
        Assertions.assertThrows(UnauthenticatedException.class,
                () -> teamBlogOwnerService.transferOwnership("caboom", 1L, "Test-uuid"));
    }

    @Test
    @DisplayName("블로그 소유자 위임 실패 - 팀블로그가 아님")
    void transferOwnershipFail_NotTeamBlog() {
        // given
        Blog personalBlog = Blog.ofExistingBlog(
                1L, "caboom", true, "카붐로그",
                "안녕하세요", true, null, BlogType.PERSONAL
        );
        BlogMemberMapping oldOwner = BlogMemberMapping.ofNewBlogMemberMapping(
                personalBlog, testOwner, roleOwner, "소유자"
        );
        BlogMemberMapping newOwner = BlogMemberMapping.ofNewBlogMemberMapping(
                personalBlog, testMember, roleMember, "새로운 소유자가 될 멤버"
        );
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(oldOwner);
        Mockito.when(blogMemberMappingRepository.findByMember_MbUuidAndBlog_BlogFid(anyString(), anyString()))
                .thenReturn(newOwner);

        // when & then
        Assertions.assertThrows(BadRequestException.class,
                () -> teamBlogOwnerService.transferOwnership("caboom", 1L, "Test-uuid"));
    }

    @Test
    @DisplayName("블로그 소유자 위임 성공")
    void transferOwnershipSuccess() {
        // given
        BlogMemberMapping oldOwner = BlogMemberMapping.ofNewBlogMemberMapping(
                testBlog, testOwner, roleOwner, "소유자"
        );
        BlogMemberMapping newOwner = BlogMemberMapping.ofNewBlogMemberMapping(
                testBlog, testMember, roleMember, "새로운 소유자가 될 멤버"
        );
        Mockito.when(blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(anyLong(), anyString()))
                .thenReturn(oldOwner);
        Mockito.when(blogMemberMappingRepository.findByMember_MbUuidAndBlog_BlogFid(anyString(), anyString()))
                .thenReturn(newOwner);

        // when & then
        Assertions.assertDoesNotThrow(
                () -> teamBlogOwnerService.transferOwnership("caboom", 1L, "Test-uuid"));
    }
}