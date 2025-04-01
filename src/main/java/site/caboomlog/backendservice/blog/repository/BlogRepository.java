package site.caboomlog.backendservice.blog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.backendservice.blog.entity.Blog;

import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, String> {

    Optional<Blog> findByBlogFid(String blogFid);
}
