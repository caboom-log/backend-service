package site.caboomlog.backendservice.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import site.caboomlog.backendservice.post.entity.Post;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
    @Query("SELECT p FROM Post p JOIN FETCH p.writer WHERE p.postId = ?1")
    Optional<Post> findByPostId(Long postId);

    int countByPostPublic(boolean postPublic);

    int countByPostPublicAndBlog_BlogFid(boolean postPublic, String blogFid);
}
