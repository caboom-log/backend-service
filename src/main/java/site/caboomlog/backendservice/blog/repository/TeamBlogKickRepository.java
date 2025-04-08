package site.caboomlog.backendservice.blog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.backendservice.blog.entity.TeamBlogKick;

public interface TeamBlogKickRepository extends JpaRepository<TeamBlogKick, Long> {
}
