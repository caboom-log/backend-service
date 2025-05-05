package site.caboomlog.backendservice.common.image.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.caboomlog.backendservice.common.annotation.LoginMember;
import site.caboomlog.backendservice.common.dto.ApiResponse;
import site.caboomlog.backendservice.common.image.dto.ImageDto;
import site.caboomlog.backendservice.common.image.service.ImageUploadService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageUploadController {
    private final ImageUploadService imageUploadService;

    @PostMapping
    public ResponseEntity<ApiResponse<ImageDto>> upload(@LoginMember Long mbNo,
                                                      @RequestPart("file") MultipartFile file) throws Exception {
        ImageDto image = imageUploadService.uploadFile(mbNo, file);
        return ResponseEntity.ok()
                .body(ApiResponse.ok(image));
    }
}
