package site.caboomlog.backendservice.post.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.caboomlog.backendservice.common.dto.ApiResponse;
import site.caboomlog.backendservice.post.exception.PostNotFoundException;

@Slf4j
@RestControllerAdvice
public class PostControllerAdvice {
    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handlePostNotFoundException(PostNotFoundException e) {
        log.info("handlePostNotFoundException - ", e);
        return ResponseEntity.status(404)
                .body(ApiResponse.notFound(e.getMessage()));
    }
}
