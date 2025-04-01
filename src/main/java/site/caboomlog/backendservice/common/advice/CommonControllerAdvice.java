package site.caboomlog.backendservice.common.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.caboomlog.backendservice.common.dto.ErrorResponse;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.member.exception.MemberNotFoundException;
import site.caboomlog.backendservice.role.exception.RoleNotFoundException;

@RestControllerAdvice
@Slf4j
public class CommonControllerAdvice {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.warn("handleException - ", e);
        return ResponseEntity.status(500)
                .body(new ErrorResponse(500, "Error occured. Check server log"));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException e) {
        log.info("handleBadRequestException - ", e);
        return ResponseEntity.status(400)
                .body(new ErrorResponse(400, e.getMessage()));
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRoleNotFoundException(RoleNotFoundException e) {
        log.info("handleRoleNotFoundException - ", e);
        return ResponseEntity.status(404)
                .body(new ErrorResponse(404, e.getMessage()));
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMemberNotFoundException(MemberNotFoundException e) {
        log.info("handleMemberNotFoundException - ", e);
        return ResponseEntity.status(404)
                .body(new ErrorResponse(404, e.getMessage()));
    }
}
