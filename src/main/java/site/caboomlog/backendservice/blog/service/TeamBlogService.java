package site.caboomlog.backendservice.blog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.caboomlog.backendservice.blog.entity.Blog;
import site.caboomlog.backendservice.blog.entity.BlogType;
import site.caboomlog.backendservice.blog.entity.TeamBlogInvite;
import site.caboomlog.backendservice.blog.entity.TeamBlogInviteStatus;
import site.caboomlog.backendservice.blog.exception.BlogMemberNicknameConflictException;
import site.caboomlog.backendservice.blog.exception.BlogNotFoundException;
import site.caboomlog.backendservice.blog.repository.BlogRepository;
import site.caboomlog.backendservice.blog.repository.TeamBlogInviteRepository;
import site.caboomlog.backendservice.blogmember.BlogMemberMapping;
import site.caboomlog.backendservice.blogmember.repository.BlogMemberMappingRepository;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.member.entity.Member;
import site.caboomlog.backendservice.member.exception.MemberNotFoundException;
import site.caboomlog.backendservice.role.entity.Role;
import site.caboomlog.backendservice.role.repository.RoleRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TeamBlogService {
    private final BlogRepository blogRepository;
    private final TeamBlogInviteRepository teamBlogInviteRepository;
    private final BlogMemberMappingRepository blogMemberMappingRepository;
    private final RoleRepository roleRepository;

    /**
     * 팀 블로그 초대 수락 로직을 처리합니다.
     *
     * <p>아래 조건을 모두 만족해야 가입 처리가 완료됩니다:</p>
     * <ul>
     *   <li>해당 블로그가 존재하고 팀 블로그일 것</li>
     *   <li>유저에게 해당 블로그 초대가 존재하고, 상태가 PENDING일 것</li>
     *   <li>블로그 내 닉네임이 중복되지 않을 것</li>
     *   <li>이미 가입된 상태가 아닐 것</li>
     * </ul>
     *
     * <p>요건을 만족하면 초대 상태를 ACCEPTED로 변경하고,
     * blog_member_mappings 테이블에 새로운 멤버 매핑을 생성합니다.</p>
     *
     * @param blogFid 블로그 식별자
     * @param mbNo 수락하는 유저의 회원 번호
     * @param blogMbNickname 블로그 내에서 사용할 닉네임
     * @throws BlogNotFoundException 블로그가 존재하지 않을 경우
     * @throws BadRequestException 초대가 없거나 유효하지 않거나, 이미 가입된 상태일 경우
     * @throws BlogMemberNicknameConflictException 블로그 닉네임이 중복된 경우
     */
    @Transactional
    public void joinTeamBlog(String blogFid, Long mbNo, String blogMbNickname) {
        Optional<Blog> optionalBlog = blogRepository.findByBlogFid(blogFid);
        if (optionalBlog.isEmpty()) {
            throw new BlogNotFoundException(String.format("존재하지 않는 블로그입니다: %s", blogFid));
        }
        Blog blog = optionalBlog.get();
        if (!blog.getBlogType().equals(BlogType.TEAM)) {
            throw new BadRequestException("블로그가 팀 블로그일 경우에만 가입할 수 있습니다.");
        }

        TeamBlogInvite teamBlogInvite = teamBlogInviteRepository.findByMember_MbNoAndBlog_BlogFid(mbNo, blogFid);
        if (teamBlogInvite == null) {
            throw new BadRequestException("초대 기록이 존재하지 않습니다.");
        }

        if (!teamBlogInvite.getStatus().equals(TeamBlogInviteStatus.PENDING)) {
            throw new BadRequestException("요청을 수락할 수 있는 상태가 아닙니다.");
        }

        teamBlogInvite.acceptInvitation();
        teamBlogInviteRepository.save(teamBlogInvite);

        Member member = teamBlogInvite.getMember();
        if (member == null) {
            throw new MemberNotFoundException("존재하지 않는 회원입니다.");
        }
        Role roleMember = roleRepository.findByRoleId("ROLE_MEMBER");

        BlogMemberMapping mapping = blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(mbNo, blogFid);
        if (mapping != null &&
                (mapping.getRole().getRoleId().equals("ROLE_MEMBER") ||
                        mapping.getRole().getRoleId().equalsIgnoreCase("ROLE_OWNER"))) {
            throw new BadRequestException("이미 가입된 회원입니다.");
        }

        if (blogMemberMappingRepository.existsByBlog_BlogFidAndMbNickname(blogFid, blogMbNickname)) {
            throw new BlogMemberNicknameConflictException(String.format("이미 사용 중인 닉네임입니다: %s", blogMbNickname));
        }
        BlogMemberMapping blogMemberMapping = BlogMemberMapping.ofNewBlogMemberMapping(blog, member, roleMember, blogMbNickname);
        blogMemberMappingRepository.save(blogMemberMapping);

    }
}
