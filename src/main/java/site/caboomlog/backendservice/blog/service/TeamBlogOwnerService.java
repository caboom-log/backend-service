package site.caboomlog.backendservice.blog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.caboomlog.backendservice.blog.dto.TeamBlogMemberResponse;
import site.caboomlog.backendservice.blog.dto.TeamBlogMembersResponse;
import site.caboomlog.backendservice.blog.entity.Blog;
import site.caboomlog.backendservice.blog.entity.BlogType;
import site.caboomlog.backendservice.blog.entity.TeamBlogInvite;
import site.caboomlog.backendservice.blog.exception.BlogNotFoundException;
import site.caboomlog.backendservice.blog.repository.TeamBlogInviteRepository;
import site.caboomlog.backendservice.blog.repository.TeamBlogKickRepository;
import site.caboomlog.backendservice.blogmember.BlogMemberMapping;
import site.caboomlog.backendservice.blogmember.repository.BlogMemberMappingRepository;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.common.notification.entity.NotificationType;
import site.caboomlog.backendservice.common.notification.event.NotificationCreatedEvent;
import site.caboomlog.backendservice.common.notification.entity.Notification;
import site.caboomlog.backendservice.common.notification.repository.NotificationRepository;
import site.caboomlog.backendservice.common.notification.repository.NotificationTypeRepository;
import site.caboomlog.backendservice.member.entity.Member;
import site.caboomlog.backendservice.member.exception.MemberNotFoundException;
import site.caboomlog.backendservice.member.repository.MemberRepository;
import site.caboomlog.backendservice.role.repository.RoleRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamBlogOwnerService {

    private final BlogMemberMappingRepository blogMemberMappingRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final TeamBlogInviteRepository teamBlogInviterRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TeamBlogMemberKicker teamBlogMemberKicker;

    /**
     * 팀 블로그에 회원을 초대합니다.
     *
     * <p>초대자는 반드시 해당 블로그의 소유자여야 하며, 초대 대상자는 유효한 회원이어야 합니다.
     * 초대 정보는 DB에 저장되며, 알림이 생성되어 실시간 전송됩니다.</p>
     *
     * @param ownerMbNo 블로그 소유자의 회원 번호
     * @param inviteeMbNo 초대 대상자의 회원 번호
     * @param blogFid 블로그 식별자
     */
    @Transactional
    public void inviteMember(Long ownerMbNo, Long inviteeMbNo, String blogFid) {
        Blog blog = getOwnedTeamBlog(ownerMbNo, blogFid);
        Member invitee = getTargetMember(inviteeMbNo);

        TeamBlogInvite teamBlogInvite = TeamBlogInvite.ofNewTeamBlogInvite(
                blog,
                invitee
        );
        teamBlogInviterRepository.save(teamBlogInvite);

        Notification notification = createInviteNotification(ownerMbNo, invitee, blog, teamBlogInvite);
        notificationRepository.save(notification);

        eventPublisher.publishEvent(new NotificationCreatedEvent(inviteeMbNo, notification.getMessage()));
    }

    /**
     * 팀 블로그의 일반 멤버 목록을 조회합니다.
     *
     * <p>해당 블로그의 소유자 권한을 확인한 뒤, ROLE_MEMBER 권한을 가진 멤버들을 조회하여 응답합니다.</p>
     *
     * @param blogFid 블로그 식별자 (URL용 고유 키)
     * @param ownerMbNo 블로그 소유자의 회원 번호
     * @return 팀 블로그 멤버 목록 응답 DTO
     */
    public TeamBlogMembersResponse getMembers(String blogFid, Long ownerMbNo) {
        List<TeamBlogMemberResponse> members = blogMemberMappingRepository
                .findTeamBlogMemberInfo(ownerMbNo, blogFid, "ROLE_MEMBER");
        return new TeamBlogMembersResponse(members);
    }

    /**
     * 특정 팀 블로그 멤버를 추방합니다.
     *
     * <p>블로그 소유자 권한을 확인한 후, 지정된 멤버를 팀 블로그에서 추방하고
     * 추방 알림을 발송합니다. 추방 처리는 TeamBlogMemberKicker를 통해 별도 트랜잭션으로 수행됩니다.</p>
     *
     * @param blogFid 블로그 식별자
     * @param ownerMbNo 블로그 소유자의 회원 번호
     * @param mbNo 추방 대상자의 회원 번호
     * @throws BadRequestException 이미 추방되었거나 권한이 없는 경우
     */
    @Transactional(readOnly = true)
    public void kickMember(String blogFid, Long ownerMbNo, Long mbNo) {
        Blog blog = getOwnedTeamBlog(ownerMbNo, blogFid);
        Member owner = getTargetMember(ownerMbNo);
        BlogMemberMapping blogMemberMapping = blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(
                mbNo, blogFid
        );
        teamBlogMemberKicker.kickSingleMember(blog, owner, blogMemberMapping);
    }

    /**
     * 해당 팀 블로그에 속한 모든 일반 멤버(ROLE_MEMBER)를 일괄 추방합니다.
     *
     * <p>각 멤버는 별도의 트랜잭션(@REQUIRES_NEW)으로 추방 처리되며,
     * 일부 추방이 실패하더라도 나머지 멤버의 추방은 계속 진행됩니다.
     * 알림도 각 멤버별로 개별 전송됩니다.</p>
     *
     * @param blogFid 블로그 식별자
     * @param ownerMbNo 블로그 소유자의 회원 번호
     */
    @Transactional(readOnly = true)
    public void kickAllMembers(String blogFid, Long ownerMbNo) {
        Blog blog = getOwnedTeamBlog(ownerMbNo, blogFid);
        Member owner = getTargetMember(ownerMbNo);
        List<BlogMemberMapping> mappings = blogMemberMappingRepository.findAllByBlog_BlogFidAndRole_RoleId(blogFid, "ROLE_MEMBER");
        for (BlogMemberMapping blogMemberMapping : mappings) {
            teamBlogMemberKicker.kickSingleMember(blog, owner, blogMemberMapping);
        }
    }

    /**
     * 팀 블로그의 소유자를 지정된 멤버로 변경합니다.
     *
     * <p>현재 소유자와 새 소유자 둘 다 블로그 멤버여야 하며,
     * 내부적으로 두 멤버의 역할(Role)을 서로 교환합니다.</p>
     *
     * @param blogFid 블로그 식별자
     * @param ownerMbNo 현재 소유자의 회원 번호
     * @param newOwnerMbNo 새로운 소유자의 회원 번호
     * @throws BadRequestException 유효하지 않은 권한이거나 팀 블로그가 아닐 경우
     */
    @Transactional
    public void transferOwnership(String blogFid, Long ownerMbNo, Long newOwnerMbNo) {
        BlogMemberMapping ownerMapping = blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(ownerMbNo, blogFid);
        BlogMemberMapping newOwnerMapping = blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(newOwnerMbNo, blogFid);
        BlogMemberMapping.transferOwnership(ownerMapping, newOwnerMapping);
    }

    /**
     * 주어진 회원 번호와 블로그 FID를 기반으로, 해당 블로그가 요청자의 소유이고 팀 블로그인지 검증합니다.
     *
     * @param ownerMbNo 블로그 소유자 회원 번호
     * @param blogFid 블로그 식별자
     * @return 블로그 객체
     * @throws BlogNotFoundException 블로그 또는 맵핑이 존재하지 않는 경우
     * @throws BadRequestException 요청자가 소유자가 아니거나, 팀 블로그가 아닌 경우
     */
    private Blog getOwnedTeamBlog(Long ownerMbNo, String blogFid) {
        BlogMemberMapping blogMemberMapping = blogMemberMappingRepository
                .findByMember_MbNoAndBlog_BlogFid(ownerMbNo, blogFid);
        if (blogMemberMapping == null) {
            throw new BlogNotFoundException("존재하지 않는 멤버 또는 블로그입니다.");
        }
        if (!blogMemberMapping.getRole().getRoleId().equalsIgnoreCase("ROLE_OWNER")) {
            throw new BadRequestException("블로그 멤버 초대는 블로그 소유자만 가능합니다.");
        }
        Blog blog = blogMemberMapping.getBlog();
        if (blog.getBlogType() != BlogType.TEAM) {
            throw new BadRequestException("멤버 초대는 팀 블로그만 가능합니다.");
        }
        return blog;
    }

    /**
     * 초대 또는 추방 대상자의 회원 정보를 조회합니다.
     *
     * @param mbNo 초대 또는 추방 대상자의 회원 번호
     * @return 회원 객체
     * @throws MemberNotFoundException 존재하지 않는 회원 번호인 경우
     */
    private Member getTargetMember(Long mbNo) {
        return memberRepository.findById(mbNo)
                .orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원입니다. mbNo: " + mbNo));
    }

    /**
     * 초대 알림(Notification)을 생성합니다.
     *
     * <p>초대자는 블로그 소유자이며, 초대 대상자는 알림을 받을 회원입니다.
     * 알림 타입은 "INVITE"로 고정되며, 메시지는 블로그 이름과 함께 포맷팅됩니다.</p>
     *
     * @param ownerMbNo 블로그 소유자의 회원 번호
     * @param invitee 알림을 받을 회원 (초대 대상자)
     * @param blog 블로그 객체
     * @param teamBlogInvite 저장된 초대 엔티티
     * @return 알림(Notification) 객체
     */
    private Notification createInviteNotification(Long ownerMbNo, Member invitee,
                                                  Blog blog, TeamBlogInvite teamBlogInvite) {
        NotificationType invite = notificationTypeRepository.findByNotificationTypeName("INVITE");
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
