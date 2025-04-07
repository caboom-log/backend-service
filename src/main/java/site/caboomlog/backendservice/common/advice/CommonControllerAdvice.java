package site.caboomlog.backendservice.common.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.caboomlog.backendservice.common.dto.ApiResponse;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.common.exception.DatabaseException;
import site.caboomlog.backendservice.common.exception.UnAuthenticatedException;
import site.caboomlog.backendservice.common.exception.UnauthorizedException;
import site.caboomlog.backendservice.member.exception.MemberNotFoundException;
import site.caboomlog.backendservice.member.exception.MemberWithdrawException;
import site.caboomlog.backendservice.role.exception.RoleNotFoundException;

@RestControllerAdvice
@Slf4j
public class CommonControllerAdvice {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.warn("handleException - ", e);
        return ResponseEntity.status(500)
                .body(ApiResponse.error(500, "서버에서 오류가 발생했습니다."));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequestException(BadRequestException e) {
        log.info("handleBadRequestException - ", e);
        return ResponseEntity.status(400)
                .body(ApiResponse.badRequest(e.getMessage()));
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoleNotFoundException(RoleNotFoundException e) {
        log.info("handleRoleNotFoundException - ", e);
        return ResponseEntity.status(500)
                .body(ApiResponse.error(500, "서버에 오류가 발생했습니다."));
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberNotFoundException(MemberNotFoundException e) {
        log.info("handleMemberNotFoundException - ", e);
        return ResponseEntity.status(404)
                .body(ApiResponse.notFound(e.getMessage()));
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleDatabaseExfeption(DatabaseException e) {
        log.warn("handleException - ", e);
        return ResponseEntity.status(500)
                .body(ApiResponse.error(500, "서버에 오류가 발생했습니다."));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(UnauthorizedException e) {
        log.info("handleUnauthorizedException - ", e);
        return ResponseEntity.status(401)
                .body(ApiResponse.error(401, e.getMessage()));
    }

    @ExceptionHandler(UnAuthenticatedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnAuthenticatedException(UnAuthenticatedException e) {
        log.info("handleUnAuthenticatedException - ", e);
        return ResponseEntity.status(403)
                .body(ApiResponse.error(403, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.info("handleMethodArgumentNotValidException - ", e);
        StringBuilder builder = new StringBuilder();
        e.getAllErrors().stream()
                .forEach(err -> builder.append(err.getDefaultMessage()).append("\n"));
        return ResponseEntity.status(400)
                .body(ApiResponse.badRequest(builder.toString()));
    }

    @ExceptionHandler(MemberWithdrawException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberWithdrawException(MemberWithdrawException e) {
        log.info("handleMemberWithdrawException - ", e);
        return ResponseEntity.status(400)
                .body(ApiResponse.badRequest(e.getMessage()));
    }
}
