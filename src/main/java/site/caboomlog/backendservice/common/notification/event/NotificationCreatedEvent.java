package site.caboomlog.backendservice.common.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class NotificationCreatedEvent {
    private final Long receiverMbNo;
    private final String message;
}
