package site.caboomlog.backendservice.blog.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.caboomlog.backendservice.blog.exception.AlreadyInvitedException;
import site.caboomlog.backendservice.common.dto.ApiResponse;

@Slf4j
@RestControllerAdvice
public class TeamBlogOwnerControllerAdvice {

    @ExceptionHandler(AlreadyInvitedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlreadyInvitedException(AlreadyInvitedException e) {
        log.info("handleAlreadyInvitedException - ", e);
        return ResponseEntity.status(409)
                .body(ApiResponse.error(409, e.getMessage()));
    }
}
