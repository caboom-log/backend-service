package site.caboomlog.backendservice.blog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.backendservice.blog.entity.TeamBlogInvite;
import site.caboomlog.backendservice.blog.entity.TeamBlogInviteStatus;

public interface TeamBlogInviteRepository extends JpaRepository<TeamBlogInvite, Long> {
    TeamBlogInvite findByMember_MbNoAndBlog_BlogFid(Long mbNo, String blogFid);

    boolean existsByMember_MbNoAndBlog_BlogFidAndAndStatus(Long mbNo, String blogFid, TeamBlogInviteStatus status);
}
