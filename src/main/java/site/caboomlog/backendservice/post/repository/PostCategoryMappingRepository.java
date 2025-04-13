package site.caboomlog.backendservice.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.backendservice.post.entity.PostCategoryMapping;

public interface PostCategoryMappingRepository extends JpaRepository<PostCategoryMapping, Long> {

}
