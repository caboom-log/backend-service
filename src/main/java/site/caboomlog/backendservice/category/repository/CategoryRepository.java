package site.caboomlog.backendservice.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.backendservice.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    long countByBlog_BlogFidAndParentCategory(String blogFid, Category parent);
}
