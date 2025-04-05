package site.caboomlog.backendservice.blog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import site.caboomlog.backendservice.member.entity.Member;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_blog_kicks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TeamBlogKick {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_blog_kick_id")
    private Long teamBlogKickId;

    @JoinColumn(name = "kicked_mb_no")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member kickedMember;

    @JoinColumn(name = "kicked_by_mb_no")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member kickedByMember;

    @JoinColumn(name = "blog_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Blog blog;

    @Column(name = "kicked_at")
    @CreationTimestamp
    private LocalDateTime kickekdAt;

    private TeamBlogKick(Long teamBlogKickId, Member kickedMember, Member kickedByMember, Blog blog, LocalDateTime kickekdAt) {
        this.teamBlogKickId = teamBlogKickId;
        this.kickedMember = kickedMember;
        this.kickedByMember = kickedByMember;
        this.blog = blog;
        this.kickekdAt = kickekdAt;
    }

    public static TeamBlogKick ofNewTeamBlogKick(Member kickedMember, Member kickedByMember, Blog blog) {
        return new TeamBlogKick(null, kickedMember, kickedByMember, blog, null);
    }
}
