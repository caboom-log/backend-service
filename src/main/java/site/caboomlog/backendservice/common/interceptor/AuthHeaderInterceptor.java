package site.caboomlog.backendservice.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import site.caboomlog.backendservice.common.exception.UnauthorizedException;

@Component
public class AuthHeaderInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String header = request.getHeader("X-Caboomlog-UID");
        if (header == null || header.isBlank()) {
            throw new UnauthorizedException("X-Caboomlog-UID 헤더 없음");
        }
        return true;
    }
}
