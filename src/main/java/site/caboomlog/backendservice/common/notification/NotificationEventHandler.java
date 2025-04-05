package site.caboomlog.backendservice.common.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import site.caboomlog.backendservice.common.notification.event.NotificationCreatedEvent;

@Component
@RequiredArgsConstructor
public class NotificationEventHandler {
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 알림 생성 이벤트를 수신하여 실시간 WebSocket 메시지를 전송합니다.
     *
     * <p>이벤트가 발생하면 알림 수신자의 ID를 기준으로 WebSocket 대상 경로를 구성하고,
     * SimpMessagingTemplate을 이용해 STOMP 메시지를 전송합니다.</p>
     *
     * <p>비동기(@Async)로 동작하므로, 이벤트 발생과는 별도로 백그라운드에서 처리됩니다.</p>
     *
     * @param event NotificationCreatedEvent 수신된 알림 생성 이벤트 객체
     */
    @EventListener
    @Async
    public void handleNotification(NotificationCreatedEvent event) {
        String destination = "/notify/notifications/" + event.getReceiverMbNo();
        messagingTemplate.convertAndSend(destination, event.getMessage());
    }
}
