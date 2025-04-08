package site.caboomlog.backendservice.common.notification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import site.caboomlog.backendservice.member.entity.Member;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @JoinColumn(name = "receiver_mb_no")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member receiverMbNo;

    @JoinColumn(name = "notification_type_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private NotificationType notificationType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "read", columnDefinition = "tinyint")
    private boolean read;

    @Column(name = "message")
    private String message;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    private Notification(Long notificationId, Member receiverMbNo, NotificationType notificationType, Long referenceId,
                         boolean read, String message, LocalDateTime createdAt) {
        this.notificationId = notificationId;
        this.receiverMbNo = receiverMbNo;
        this.notificationType = notificationType;
        this.referenceId = referenceId;
        this.read = read;
        this.message = message;
        this.createdAt = createdAt;
    }

    public static Notification ofNewNotification(Member receiverMbNo, NotificationType notificationType,
                                                 Long referenceId, String message) {
        return new Notification(null, receiverMbNo, notificationType, referenceId,
                false, message, null);
    }
}
