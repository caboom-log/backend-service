package site.caboomlog.backendservice.blog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.backendservice.blog.entity.TeamBlogInvite;

public interface TeamBlogInviteRepository extends JpaRepository<TeamBlogInvite, Long> {
}
