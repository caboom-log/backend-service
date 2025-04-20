package site.caboomlog.backendservice.common.image;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {
    String uploadFile(Long mbNo, MultipartFile file) throws Exception;
}
