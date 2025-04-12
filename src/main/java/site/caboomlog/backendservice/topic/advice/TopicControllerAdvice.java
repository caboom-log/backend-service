package site.caboomlog.backendservice.topic.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.caboomlog.backendservice.common.dto.ApiResponse;
import site.caboomlog.backendservice.topic.exception.TopicNotFoundException;

@Slf4j
@RestControllerAdvice
public class TopicControllerAdvice {
    @ExceptionHandler(TopicNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleTopicNotFoundException(TopicNotFoundException e) {
        log.info("handleTopicNotFoundException - ", e);
        return ResponseEntity.status(404)
                .body(ApiResponse.notFound(e.getMessage()));
    }
}
