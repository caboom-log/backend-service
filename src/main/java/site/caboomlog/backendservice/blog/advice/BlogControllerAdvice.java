package site.caboomlog.backendservice.blog.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.caboomlog.backendservice.blog.exception.BlogFidDuplicatedException;
import site.caboomlog.backendservice.blog.exception.BlogNotFoundException;
import site.caboomlog.backendservice.blog.exception.InvalidBlogCountRangeException;

@Slf4j
@RestControllerAdvice
public class BlogControllerAdvice {

    @ExceptionHandler(BlogNotFoundException.class)
    public ResponseEntity<String> handleBlogNotFoundException(BlogNotFoundException e) {
        log.info("handleBlogNotFoundException - ", e);
        return ResponseEntity.status(404).body(e.getMessage());
    }

    @ExceptionHandler(BlogFidDuplicatedException.class)
    public ResponseEntity<String> handleBlogFidDuplicatedException(BlogFidDuplicatedException e) {
        log.info("handleBlogFidDuplicatedException - ", e);
        return ResponseEntity.status(409).body(e.getMessage());
    }

    @ExceptionHandler(InvalidBlogCountRangeException.class)
    public ResponseEntity<String> handleInvalidBlogRangeException(InvalidBlogCountRangeException e) {
        log.info("handleInvalidBlogRangeException - ", e);
        return ResponseEntity.status(400).body(e.getMessage());
    }
}
