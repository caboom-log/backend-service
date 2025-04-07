package site.caboomlog.backendservice.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import site.caboomlog.backendservice.common.exception.UnauthorizedException;

@Component
@Slf4j
public class AuthHeaderInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getMethod().equalsIgnoreCase("GET") &&
                (request.getRequestURI().matches("/api/blogs/[^/]+$") ||
                        request.getRequestURI().matches("/api/blogs/[^/]+/members"))
        ) {
            return true;
        }

        log.debug("preHandle - X-Caboomlog-UID 헤더 검증");
        String header = request.getHeader("X-Caboomlog-UID");
        if (header == null || header.isBlank()) {
            throw new UnauthorizedException("X-Caboomlog-UID 헤더 없음");
        }
        return true;
    }
}
