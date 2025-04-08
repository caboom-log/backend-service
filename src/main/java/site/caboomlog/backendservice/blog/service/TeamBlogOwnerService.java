package site.caboomlog.backendservice.blog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.caboomlog.backendservice.blog.entity.Blog;
import site.caboomlog.backendservice.blog.entity.BlogType;
import site.caboomlog.backendservice.blog.entity.TeamBlogInvite;
import site.caboomlog.backendservice.blog.entity.TeamBlogInviteStatus;
import site.caboomlog.backendservice.blog.exception.AlreadyInvitedException;
import site.caboomlog.backendservice.blog.exception.BlogNotFoundException;
import site.caboomlog.backendservice.blog.repository.TeamBlogInviteRepository;
import site.caboomlog.backendservice.blogmember.entity.BlogMemberMapping;
import site.caboomlog.backendservice.blogmember.repository.BlogMemberMappingRepository;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.common.exception.DatabaseException;
import site.caboomlog.backendservice.common.exception.UnauthenticatedException;
import site.caboomlog.backendservice.common.notification.entity.NotificationType;
import site.caboomlog.backendservice.common.notification.event.NotificationCreatedEvent;
import site.caboomlog.backendservice.common.notification.entity.Notification;
import site.caboomlog.backendservice.common.notification.repository.NotificationRepository;
import site.caboomlog.backendservice.common.notification.repository.NotificationTypeRepository;
import site.caboomlog.backendservice.member.entity.Member;
import site.caboomlog.backendservice.member.exception.MemberNotFoundException;
import site.caboomlog.backendservice.member.exception.MemberWithdrawException;
import site.caboomlog.backendservice.member.repository.MemberRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamBlogOwnerService {

    private final BlogMemberMappingRepository blogMemberMappingRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final TeamBlogInviteRepository teamBlogInviteRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TeamBlogMemberKicker teamBlogMemberKicker;

    /**
     * 팀 블로그에 특정 회원을 초대합니다.
     * <p>
     * 초대자는 해당 블로그의 소유자여야 하며, 초대 대상자는 유효한 회원이어야 합니다.
     * 이미 초대된 상태(PENDING)인 경우 초대할 수 없습니다.
     * 초대 정보는 저장되며, 초대 대상자에게 알림이 전송됩니다.
     *
     * @param ownerMbNo        블로그 소유자의 회원 번호
     * @param inviteeMbUuid    초대 대상자의 회원 UUID
     * @param blogFid          초대할 블로그의 식별자
     */
    @Transactional
    public void inviteMember(Long ownerMbNo, String inviteeMbUuid, String blogFid) {
        Blog blog = getOwnedTeamBlog(ownerMbNo, blogFid);
        Member invitee = memberRepository.findByMbUuid(inviteeMbUuid)
                .orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원입니다."));
        if (invitee.getWithdrawalAt() != null) {
            throw new MemberWithdrawException("이미 탈퇴한 회원입니다.");
        }

        if (teamBlogInviteRepository
                .existsByMember_MbNoAndBlog_BlogFidAndAndStatus(invitee.getMbNo(), blogFid, TeamBlogInviteStatus.PENDING)) {
            throw new AlreadyInvitedException("이미 초대된 회원입니다.");
        }

        TeamBlogInvite teamBlogInvite = TeamBlogInvite.ofNewTeamBlogInvite(
                blog,
                invitee
        );
        teamBlogInviteRepository.save(teamBlogInvite);

        Notification notification = createInviteNotification(ownerMbNo, invitee, blog, teamBlogInvite);
        notificationRepository.save(notification);

        eventPublisher.publishEvent(new NotificationCreatedEvent(invitee.getMbNo(), notification.getMessage()));
    }

    /**
     * 팀 블로그에서 특정 멤버를 추방합니다.
     * <p>
     * 요청자는 블로그 소유자여야 하며, 대상 멤버는 블로그의 멤버여야 합니다.
     * 추방은 별도의 트랜잭션을 통해 수행되며, 알림도 전송됩니다.
     *
     * @param blogFid            블로그 식별자
     * @param ownerMbNo          블로그 소유자의 회원 번호
     * @param targetMbNickname   추방 대상자의 블로그 내 닉네임
     */
    @Transactional(readOnly = true)
    public void kickMember(String blogFid, Long ownerMbNo, String targetMbNickname) {
        Blog blog = getOwnedTeamBlog(ownerMbNo, blogFid);
        Member owner = memberRepository.findById(ownerMbNo)
                .orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원입니다."));
        BlogMemberMapping targetMapping = blogMemberMappingRepository
                .findByBlog_BlogFidAndMbNickname(blogFid, targetMbNickname);
        if (targetMapping == null) {
            throw new MemberNotFoundException("멤버가 존재하지 않습니다.");
        }
        teamBlogMemberKicker.kickSingleMember(blog, owner, targetMapping);
    }

    /**
     * 팀 블로그에서 모든 일반 멤버(ROLE_MEMBER)를 일괄 추방합니다.
     * <p>
     * 각 멤버는 별도의 트랜잭션으로 추방되며, 알림도 전송됩니다.
     *
     * @param blogFid    블로그 식별자
     * @param ownerMbNo  블로그 소유자의 회원 번호
     */
    @Transactional(readOnly = true)
    public void kickAllMembers(String blogFid, Long ownerMbNo) {
        Blog blog = getOwnedTeamBlog(ownerMbNo, blogFid);
        Member owner = getTargetMember(ownerMbNo);
        List<BlogMemberMapping> mappings = blogMemberMappingRepository
                .findAllByBlog_BlogFidAndRole_RoleId(blogFid, "ROLE_MEMBER");
        for (BlogMemberMapping blogMemberMapping : mappings) {
            teamBlogMemberKicker.kickSingleMember(blog, owner, blogMemberMapping);
        }
    }

    /**
     * 팀 블로그의 소유자를 지정된 멤버로 변경합니다.
     * <p>
     * 두 멤버 모두 블로그의 멤버여야 하며, 역할을 서로 교환합니다.
     *
     * @param blogFid         블로그 식별자
     * @param ownerMbNo       기존 소유자의 회원 번호
     * @param newOwnerMbUuid  새로운 소유자의 회원 UUID
     */
    @Transactional
    public void transferOwnership(String blogFid, Long ownerMbNo, String newOwnerMbUuid) {
        BlogMemberMapping ownerMapping = blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(ownerMbNo, blogFid);
        BlogMemberMapping newOwnerMapping = blogMemberMappingRepository.findByMember_MbUuidAndBlog_BlogFid(newOwnerMbUuid, blogFid);
        BlogMemberMapping.transferOwnership(ownerMapping, newOwnerMapping);
    }

    /**
     * 해당 회원이 소유하고 있는 팀 블로그를 가져옵니다.
     *
     * @param ownerMbNo 블로그 소유자 회원 번호
     * @param blogFid   블로그 식별자
     * @return 블로그 객체
     * @throws BlogNotFoundException 블로그 또는 멤버 매핑이 없을 경우
     * @throws UnauthenticatedException 소유자가 아닌 경우
     * @throws BadRequestException 팀 블로그가 아닌 경우
     */
    private Blog getOwnedTeamBlog(Long ownerMbNo, String blogFid) {
        BlogMemberMapping blogMemberMapping = blogMemberMappingRepository
                .findByMember_MbNoAndBlog_BlogFid(ownerMbNo, blogFid);
        if (blogMemberMapping == null) {
            throw new BlogNotFoundException("존재하지 않는 멤버 또는 블로그입니다.");
        }
        if (!blogMemberMapping.getRole().getRoleId().equalsIgnoreCase("ROLE_OWNER")) {
            throw new UnauthenticatedException("블로그 소유자가 아닙니다.");
        }
        Blog blog = blogMemberMapping.getBlog();
        if (blog.getBlogType() != BlogType.TEAM) {
            throw new BadRequestException("팀 블로그가 아닙니다.");
        }
        return blog;
    }

    /**
     * 회원 번호를 통해 해당 회원을 조회합니다.
     *
     * @param mbNo 회원 번호
     * @return 회원 객체
     * @throws MemberNotFoundException 존재하지 않는 회원일 경우
     */
    private Member getTargetMember(Long mbNo) {
        return memberRepository.findById(mbNo)
                .orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원입니다."));
    }

    /**
     * 초대 알림(Notification)을 생성합니다.
     *
     * @param ownerMbNo       블로그 소유자의 회원 번호
     * @param invitee         초대 대상 회원
     * @param blog            블로그 객체
     * @param teamBlogInvite  초대 정보 객체
     * @return 알림 객체
     */
    private Notification createInviteNotification(Long ownerMbNo, Member invitee,
                                                  Blog blog, TeamBlogInvite teamBlogInvite) {
        NotificationType invite = notificationTypeRepository.findByNotificationTypeName("INVITE");
        if (invite == null) {
            throw new DatabaseException("NotificationType 'INVITE' 없음");
        }
        String blogOwnerName = memberRepository
                .findById(ownerMbNo)
                .map(Member::getMbName)
                .orElseThrow(() -> new MemberNotFoundException("존재하지 않는 멤버입니다."));
        return Notification.ofNewNotification(
                invitee,
                invite,
                teamBlogInvite.getTeamBlogInviteId(),
                String.format("%s 님이 %s 가입 초대를 보냈습니다.", blogOwnerName,  blog.getBlogName())
        );
    }
}
