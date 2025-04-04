package site.caboomlog.backendservice.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 알림 발생시 웹소켓으로 클라이언트에게 메시지 보내주기 위함
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
