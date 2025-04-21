package site.caboomlog.backendservice.common.image.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.backendservice.common.image.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
