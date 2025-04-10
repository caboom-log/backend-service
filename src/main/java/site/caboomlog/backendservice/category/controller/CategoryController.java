package site.caboomlog.backendservice.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.caboomlog.backendservice.category.dto.CreateCategoryRequest;
import site.caboomlog.backendservice.category.service.CategoryService;
import site.caboomlog.backendservice.common.annotation.LoginMember;
import site.caboomlog.backendservice.common.dto.ApiResponse;

@RestController
@RequestMapping("/api/blogs/{blogFid}/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createCategory(@PathVariable("blogFid") String blogFid,
                                                      @LoginMember Long mbNo,
                                                      @RequestBody CreateCategoryRequest request) {
        categoryService.createCategory(blogFid, mbNo, request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created());
    }
}
