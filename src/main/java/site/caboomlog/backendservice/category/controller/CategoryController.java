package site.caboomlog.backendservice.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.caboomlog.backendservice.category.dto.CategoryResponse;
import site.caboomlog.backendservice.category.dto.CreateCategoryRequest;
import site.caboomlog.backendservice.category.service.CategoryService;
import site.caboomlog.backendservice.common.annotation.LoginMember;
import site.caboomlog.backendservice.common.dto.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/blogs/{blogFid}/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 블로그에 새로운 카테고리를 생성합니다.
     *
     * @param blogFid 블로그 고유 식별자
     * @param mbNo 로그인한 사용자(블로그 소유자)의 회원 번호 (AOP로 주입됨)
     * @param request 카테고리 생성 요청 본문
     * @return 생성 완료 응답 (HTTP 201 Created)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createCategory(@PathVariable("blogFid") String blogFid,
                                                      @LoginMember Long mbNo,
                                                      @RequestBody CreateCategoryRequest request) {
        categoryService.createCategory(blogFid, mbNo, request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created());
    }

    /**
     * 블로그의 전체 카테고리 목록을 트리 형태로 조회합니다.
     * 이 API는 인증이 필요하며, 블로그 소유자만 호출할 수 있습니다.
     *
     * @param blogFid 블로그 고유 식별자
     * @param mbNo 로그인한 사용자의 회원 번호 (AOP로 주입됨)
     * @return 트리 형태의 카테고리 목록
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories(@PathVariable("blogFid")
                                                                                    String blogFid,
                                                                                @LoginMember Long mbNo) {
        List<CategoryResponse> categories = categoryService.getCategories(blogFid, mbNo);
        return ResponseEntity.ok(ApiResponse.ok(categories));
    }

    /**
     * 블로그의 공개된 카테고리 목록을 트리 형태로 조회합니다.
     * 이 API는 인증이 필요하지 않으며, 누구나 접근 가능합니다.
     *
     * @param blogFid 블로그 고유 식별자
     * @return 트리 형태의 공개 카테고리 목록
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllPublicCategories(@PathVariable("blogFid")
                                                                                          String blogFid) {
        List<CategoryResponse> categories = categoryService.getAllPublicCategories(blogFid);
        return ResponseEntity.ok(ApiResponse.ok(categories));
    }
}
