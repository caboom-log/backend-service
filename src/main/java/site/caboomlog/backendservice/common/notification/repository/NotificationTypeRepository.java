package site.caboomlog.backendservice.common.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.backendservice.common.notification.entity.NotificationType;

public interface NotificationTypeRepository extends JpaRepository<NotificationType, String> {
    NotificationType findByNotificationTypeName(String name);
}
