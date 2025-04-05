package site.caboomlog.backendservice.blogmember;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import site.caboomlog.backendservice.blog.entity.Blog;
import site.caboomlog.backendservice.blog.entity.BlogType;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.member.entity.Member;
import site.caboomlog.backendservice.role.entity.Role;

import java.time.LocalDateTime;

@Entity
@Table(name = "blog_member_mappings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class BlogMemberMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blog_member_mapping_id")
    private Long blogMemberMappingId;

    @JoinColumn(name = "blog_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Blog blog;

    @JoinColumn(name = "mb_no")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @JoinColumn(name = "role_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Role role;

    @Column(name = "mb_nickname")
    private String mbNickname;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    private BlogMemberMapping(Long blogMemberMappingId, Blog blog, Member member, Role role, String mbNickname) {
        this.blogMemberMappingId = blogMemberMappingId;
        this.blog = blog;
        this.member = member;
        this.role = role;
        this.mbNickname = mbNickname;
    }

    public static BlogMemberMapping ofNewBlogMemberMapping(Blog blog, Member member, Role role, String mbNickname) {
        return new BlogMemberMapping(null, blog, member, role, mbNickname);
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    /**
     * 블로그 소유자 권한을 다른 멤버에게 위임합니다.
     *
     * <p>두 블로그 멤버 매핑 객체 간의 Role 정보를 서로 바꾸어 소유자를 변경합니다.
     * 단, 팀 블로그일 경우에만 유효하며, 소유자만 위임할 수 있습니다.</p>
     *
     * @param ownerMapping 현재 소유자의 블로그-멤버 매핑 객체
     * @param newOwnerMapping 새 소유자가 될 멤버의 블로그-멤버 매핑 객체
     * @throws BadRequestException 팀 블로그가 아닌 경우 또는 소유자가 아닌 경우
     * @throws IllegalArgumentException 두 매핑이 서로 다른 블로그일 경우
     */
    public static void transferOwnership(BlogMemberMapping ownerMapping, BlogMemberMapping newOwnerMapping) {
        if (!ownerMapping.getBlog().equals(newOwnerMapping.getBlog())) {
            throw new IllegalArgumentException("ownerMapping.getBlog(), newOwnerMapping.getBlog() 다름");
        }
        if (!ownerMapping.getBlog().getBlogType().equals(BlogType.TEAM)) {
            throw new BadRequestException("블로그 소유자 권한 위임은 팀블로그만 가능합니다.");
        }
        if (!ownerMapping.getRole().getRoleId().equalsIgnoreCase("ROLE_OWNER")) {
            throw new BadRequestException("블로그 소유자 권한 위임은 블로그 소유자만 가능합니다.");
        }
        Role roleOwner = ownerMapping.getRole();
        ownerMapping.changeRole(newOwnerMapping.getRole());
        newOwnerMapping.changeRole(roleOwner);
    }
}
