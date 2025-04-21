package site.caboomlog.backendservice.common.image.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.backendservice.common.image.entity.PostImageMapping;

public interface PostImageMappingRepository extends JpaRepository<PostImageMapping, Long> {
}
