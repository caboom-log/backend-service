package site.caboomlog.backendservice.common.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationTypeRepository extends JpaRepository<Object, String> {
    Object findByNotificationTypeName(String name);
}
