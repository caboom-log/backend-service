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
import site.caboomlog.backendservice.blog.entity.TeamBlogKick;
import site.caboomlog.backendservice.blog.repository.TeamBlogKickRepository;
import site.caboomlog.backendservice.blogmember.entity.BlogMemberMapping;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.common.exception.DatabaseException;
import site.caboomlog.backendservice.common.notification.entity.NotificationType;
import site.caboomlog.backendservice.common.notification.event.NotificationCreatedEvent;
import site.caboomlog.backendservice.common.notification.repository.NotificationRepository;
import site.caboomlog.backendservice.common.notification.repository.NotificationTypeRepository;
import site.caboomlog.backendservice.member.entity.Member;
import site.caboomlog.backendservice.role.entity.Role;
import site.caboomlog.backendservice.role.repository.RoleRepository;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class TeamBlogMemberKickerTest {

    @Mock
    NotificationRepository notificationRepository;
    @Mock
    NotificationTypeRepository notificationTypeRepository;
    @Mock
    TeamBlogKickRepository teamBlogKickRepository;
    @Mock
    RoleRepository roleRepository;
    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    TeamBlogMemberKicker teamBlogMemberKicker;

    Member testOwner = Member.ofExistingMember(1L, "caboom@test.com", "세연", "1234qwer", "010-1111-1111", null, null);
    Member testMember = Member.ofExistingMember(2L, "test@test.com", "멤버1", "1234qwer", "010-2222-2222", null, null);
    Blog testBlog = Blog.ofExistingBlog(1L, "caboom", true,
            "카붐로그", "안녕하세요", true, null, BlogType.TEAM);
    Role roleMember = Role.ofNewRole("ROLE_MEMBER", "블로그_멤버", "팀 블로그 멤버입니다.");
    Role roleKicked = Role.ofNewRole("ROLE_KICKED", "블로그_추방멤버", "추방된 멤버입니다.");
    NotificationType kicked = new NotificationType();


    @Test
    @DisplayName("단일 멤버 추방 실패 - 이미 추방된 멤버")
    void kickSingleMemberFail_AlreadyKicked() {
        // given
        BlogMemberMapping mapping = BlogMemberMapping
                .ofNewBlogMemberMapping(testBlog, testMember, roleKicked, "추방멤");

        Mockito.when(teamBlogKickRepository.save(any()))
                .thenReturn(TeamBlogKick.ofNewTeamBlogKick(testMember, testOwner, testBlog));
        Mockito.when(roleRepository.findByRoleId("ROLE_KICKED"))
                .thenReturn(roleKicked);

        // when & then
        Assertions.assertThrows(BadRequestException.class,
                () -> teamBlogMemberKicker.kickSingleMember(testBlog, testOwner, mapping));
    }

    @Test
    @DisplayName("단일 멤버 추방 실패 - 탈퇴한 멤버")
    void kickSingleMemberFail_WithdrawnMember() throws Exception {
        // given
        Field withdrawalAtField = Member.class.getDeclaredField("withdrawalAt");
        withdrawalAtField.setAccessible(true);
        withdrawalAtField.set(testMember, LocalDateTime.now());
        BlogMemberMapping mapping = BlogMemberMapping
                .ofNewBlogMemberMapping(testBlog, testMember, roleMember, "추방멤");
        Mockito.when(teamBlogKickRepository.save(any()))
                .thenReturn(TeamBlogKick.ofNewTeamBlogKick(testMember, testOwner, testBlog));
        Mockito.when(roleRepository.findByRoleId("ROLE_KICKED"))
                .thenReturn(roleKicked);

        // when & then
        Assertions.assertThrows(BadRequestException.class,
                () -> teamBlogMemberKicker.kickSingleMember(testBlog, testOwner, mapping));
    }

    @Test
    @DisplayName("단일 멤버 추방 실패 - 데이터베이스에 ROLE_KICKED 없음")
    void kickSingleMemberFail_DatabaseException() throws Exception {
        // given
        BlogMemberMapping mapping = BlogMemberMapping
                .ofNewBlogMemberMapping(testBlog, testMember, roleMember, "추방멤");
        Mockito.when(teamBlogKickRepository.save(any()))
                .thenReturn(TeamBlogKick.ofNewTeamBlogKick(testMember, testOwner, testBlog));
        Mockito.when(roleRepository.findByRoleId("ROLE_KICKED"))
                .thenReturn(null);

        // when & then
        Assertions.assertThrows(DatabaseException.class,
                () -> teamBlogMemberKicker.kickSingleMember(testBlog, testOwner, mapping));
    }

    @Test
    @DisplayName("단일 멤버 추방 실패 - NotoficationType 데이터베이스에 데이터 x ")
    void kickSingleMemberFail_DatabaseException2() {
        // given
        BlogMemberMapping mapping = BlogMemberMapping
                .ofNewBlogMemberMapping(testBlog, testMember, roleMember, "추방멤");
        Mockito.when(teamBlogKickRepository.save(any()))
                .thenReturn(TeamBlogKick.ofNewTeamBlogKick(testMember, testOwner, testBlog));
        Mockito.when(roleRepository.findByRoleId("ROLE_KICKED"))
                .thenReturn(roleKicked);
        Mockito.when(notificationTypeRepository.findByNotificationTypeName(anyString()))
                .thenReturn(null);

        // when & then
        Assertions.assertThrows(DatabaseException.class,
                () -> teamBlogMemberKicker.kickSingleMember(testBlog, testOwner, mapping));
    }

    @Test
    @DisplayName("단일 멤버 추방 성공")
    void kickSingleMemberSuccess() {
        // given
        BlogMemberMapping mapping = BlogMemberMapping
                .ofNewBlogMemberMapping(testBlog, testMember, roleMember, "추방멤");
        Mockito.when(teamBlogKickRepository.save(any()))
                .thenReturn(TeamBlogKick.ofNewTeamBlogKick(testMember, testOwner, testBlog));
        Mockito.when(roleRepository.findByRoleId("ROLE_KICKED"))
                .thenReturn(roleKicked);
        Mockito.when(notificationTypeRepository.findByNotificationTypeName(anyString()))
                .thenReturn(kicked);


        // when
        teamBlogMemberKicker.kickSingleMember(testBlog, testOwner, mapping);

        // then
        Assertions.assertEquals(mapping.getRole(), roleKicked);
        Mockito.verify(teamBlogKickRepository, Mockito.times(1)).save(any());
        Mockito.verify(notificationRepository, Mockito.times(1)).save(any());
        Mockito.verify(eventPublisher, Mockito.times(1))
                .publishEvent(any(NotificationCreatedEvent.class));
    }
}