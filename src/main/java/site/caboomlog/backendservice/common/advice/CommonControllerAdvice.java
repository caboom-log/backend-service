package site.caboomlog.backendservice.common.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.caboomlog.backendservice.common.dto.ErrorResponse;
import site.caboomlog.backendservice.common.exception.BadRequestException;

@RestControllerAdvice
@Slf4j
public class CommonControllerAdvice {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException e) {
        log.info("handleBadRequestException - ", e);
        return ResponseEntity.status(400)
                .body(new ErrorResponse(400, e.getMessage()));
    }
}
