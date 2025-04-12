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
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        if (request.getMethod().equalsIgnoreCase("GET") &&
                (uri.matches("/api/blogs/[^/]+$") ||
                uri.matches("/api/blogs/[^/]+/members") ||
                uri.matches("/api/topics") ||
                uri.matches("/api/blogs/[^/]+/categories/public") ||
                uri.matches("/api/blogs/[^/]+/posts/public"))
        ) {
            return true;
        }

        log.debug("preHandle - X-Caboomlog-UID 헤더 검증");
        String header = request.getHeader("X-Caboomlog-UID");
        if (header == null || header.isBlank()) {
            throw new UnauthorizedException("X-Caboomlog-UID 헤더 없음, uri: " + uri);
        }
        return true;
    }
}
