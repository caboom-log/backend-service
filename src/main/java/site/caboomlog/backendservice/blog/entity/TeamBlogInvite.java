package site.caboomlog.backendservice.blog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import site.caboomlog.backendservice.member.entity.Member;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_blog_invites")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TeamBlogInvite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_blog_invite_id")
    private Long teamBlogInviteId;

    @JoinColumn(name = "blog_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Blog blog;

    @JoinColumn(name = "mb_no")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TeamBlogInviteStatus status;

    @Column(name = "invited_at")
    @CreationTimestamp
    private LocalDateTime invitedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    public TeamBlogInvite(Long teamBlogInviteId, Blog blog, Member member, TeamBlogInviteStatus status,
                          LocalDateTime invitedAt, LocalDateTime respondedAt) {
        this.teamBlogInviteId = teamBlogInviteId;
        this.blog = blog;
        this.member = member;
        this.status = status;
        this.invitedAt = invitedAt;
        this.respondedAt = respondedAt;
    }

    public static TeamBlogInvite ofNewTeamBlogInvite(Blog blog, Member member) {
        return new TeamBlogInvite(null, blog, member, TeamBlogInviteStatus.PENDING, null, null);
    }

    public void acceptInvitation() {
        this.respondedAt = LocalDateTime.now();
        this.status = TeamBlogInviteStatus.ACCEPTED;
    }
}
