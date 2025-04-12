package site.caboomlog.backendservice.category.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.caboomlog.backendservice.category.exception.CategoryNotFoundException;
import site.caboomlog.backendservice.common.dto.ApiResponse;

@Slf4j
@RestControllerAdvice
public class CategoryControllerAdvice {
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCategoryNotFoundException(CategoryNotFoundException e) {
        log.info("handleCategoryNotFoundException - ", e);
        return ResponseEntity.status(404)
                .body(ApiResponse.notFound(e.getMessage()));
    }
}
