package site.caboomlog.backendservice.blog.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.caboomlog.backendservice.blog.exception.BlogNotFoundException;

@RestControllerAdvice
public class BlogControllerAdvice {

    @ExceptionHandler(BlogNotFoundException.class)
    public ResponseEntity<String> handleBlogNotFoundException(BlogNotFoundException e) {
        return ResponseEntity.status(404).body(e.getMessage());
    }
}
