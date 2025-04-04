package site.caboomlog.backendservice.common.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class NotificationCreatedEvent {
    private final Long receiverMbNo;
    private final String message;
}
