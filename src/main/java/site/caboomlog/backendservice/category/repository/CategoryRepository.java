package site.caboomlog.backendservice.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import site.caboomlog.backendservice.category.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    long countByBlog_BlogFidAndParentCategory(String blogFid, Category parent);

    @Query("SELECT c FROM Category c JOIN FETCH c.topic WHERE c.blog.blogFid = ?1 ORDER BY c.categoryOrder ASC")
    List<Category> findAllByBlog_BlogFid(String blogFid);

    @Query("SELECT c FROM Category c JOIN FETCH c.topic WHERE c.blog.blogFid = ?1 AND c.categoryPublic = true ORDER BY c.categoryOrder ASC")
    List<Category> findAllPublicByBlog_BlogFid(String blogFid);

    @Query("SELECT c FROM Category c JOIN FETCH c.blog WHERE c.categoryId = ?1")
    Optional<Category> findByCategoryId(long categoryId);

}
