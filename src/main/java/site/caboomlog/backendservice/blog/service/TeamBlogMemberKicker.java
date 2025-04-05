package site.caboomlog.backendservice.blog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import site.caboomlog.backendservice.blog.entity.Blog;
import site.caboomlog.backendservice.blog.entity.TeamBlogKick;
import site.caboomlog.backendservice.blog.repository.TeamBlogKickRepository;
import site.caboomlog.backendservice.blogmember.BlogMemberMapping;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.common.notification.entity.Notification;
import site.caboomlog.backendservice.common.notification.entity.NotificationType;
import site.caboomlog.backendservice.common.notification.event.NotificationCreatedEvent;
import site.caboomlog.backendservice.common.notification.repository.NotificationRepository;
import site.caboomlog.backendservice.common.notification.repository.NotificationTypeRepository;
import site.caboomlog.backendservice.member.entity.Member;
import site.caboomlog.backendservice.role.entity.Role;
import site.caboomlog.backendservice.role.repository.RoleRepository;

@Component
@RequiredArgsConstructor
public class TeamBlogMemberKicker {
    private final NotificationRepository notificationRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final TeamBlogKickRepository teamBlogKickRepository;
    private final RoleRepository roleRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 단일 멤버를 팀 블로그에서 추방합니다.
     *
     * <p>추방 기록을 저장하고, 멤버의 역할을 KICKED로 변경하며, 알림을 전송합니다.
     * 이 메서드는 별도 트랜잭션(@REQUIRES_NEW)으로 처리되어, 독립적인 추방 단위로 동작합니다.</p>
     *
     * @param blog 블로그 객체
     * @param owner 블로그 소유자
     * @param blogMemberMapping 추방 대상자의 블로그 멤버 매핑 정보
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void kickSingleMember(Blog blog, Member owner, BlogMemberMapping blogMemberMapping) {
        TeamBlogKick teamBlogKick = teamBlogKickRepository.save(TeamBlogKick.ofNewTeamBlogKick(
                owner,
                blogMemberMapping.getMember(),
                blog));
        Role kicked = roleRepository.findByRoleId("ROLE_KICKED");
        if (blogMemberMapping.getRole().equals(kicked)) {
            throw new BadRequestException("추방할 수 없거나 이미 추방된 회원입니다.");
        }
        blogMemberMapping.changeRole(kicked);
        Notification notification = createKickedNotification(
                blogMemberMapping.getMember(), blog.getBlogName(), teamBlogKick);
        notificationRepository.save(notification);
        eventPublisher.publishEvent(new NotificationCreatedEvent(blogMemberMapping.getMember().getMbNo(),
                notification.getMessage()));
    }

    /**
     * 추방 알림을 생성합니다.
     *
     * <p>알림은 알림 타입 "KICKED"로 생성되며, 알림 메시지에는 블로그 이름이 포함됩니다.</p>
     *
     * @param targetMember 추방 대상자
     * @param blogName 블로그 이름
     * @param teamBlogKick 추방 이력 엔티티
     * @return 생성된 알림(Notification)
     */
    private Notification createKickedNotification(Member targetMember, String blogName,
                                                  TeamBlogKick teamBlogKick) {
        NotificationType kicked = notificationTypeRepository.findByNotificationTypeName("KICKED");
        return Notification.ofNewNotification(
                targetMember,
                kicked,
                teamBlogKick.getTeamBlogKickId(),
                String.format("팀블로그 %s 로부터 추방당했습니다.", blogName)
        );
    }
}
