package site.caboomlog.backendservice.member.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.caboomlog.backendservice.member.exception.MainBlogNotFoundException;

@Slf4j
@RestControllerAdvice
public class MemberControllerAdvice {

    @ExceptionHandler(MainBlogNotFoundException.class)
    public ResponseEntity<String> handleMainBlogNotFoundException(MainBlogNotFoundException e) {
        log.error("handleMainBlogNotFoundException - 메인 블로그 존재하지 않음, ", e);
        return ResponseEntity.internalServerError().body(e.getMessage());
    }
}
