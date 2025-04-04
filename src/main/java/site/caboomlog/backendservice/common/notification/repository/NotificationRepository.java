package site.caboomlog.backendservice.common.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.backendservice.common.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
