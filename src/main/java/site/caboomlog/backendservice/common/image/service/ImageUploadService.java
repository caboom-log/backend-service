package site.caboomlog.backendservice.common.image.service;

import org.springframework.web.multipart.MultipartFile;
import site.caboomlog.backendservice.common.image.dto.ImageDto;

public interface ImageUploadService {
    ImageDto uploadFile(Long mbNo, MultipartFile file) throws Exception;
    ImageDto uploadBlogMainImage(String blogFid, MultipartFile file) throws Exception;
    void deleteFile(String url);
}
