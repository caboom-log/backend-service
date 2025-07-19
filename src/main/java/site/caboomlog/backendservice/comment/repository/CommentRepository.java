package site.caboomlog.backendservice.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.backendservice.comment.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findAllByPost_PostId(Long postId, Pageable pageable);
}
