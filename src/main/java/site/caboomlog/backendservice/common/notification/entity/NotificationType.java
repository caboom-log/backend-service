package site.caboomlog.backendservice.common.notification.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "notification_types")
@Getter
public class NotificationType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_type_id")
    private String notificationTypeId;

    @Column(name = "notification_type_name")
    private String notificationTypeName;

    @Column(name = "notification_type_description")
    private String notificationTypeDescription;
}
