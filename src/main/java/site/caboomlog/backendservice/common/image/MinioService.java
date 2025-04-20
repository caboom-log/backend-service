package site.caboomlog.backendservice.common.image;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioService implements ImageUploadService {
    @Value("${minio.url}")
    private String url;

    @Value("${minio.bucket}")
    private String bucketName;

    private final MinioClient minioClient;

    @Override
    public String uploadFile(Long mbNo, MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        String filePath = String.format("%d/%d/%02d/%02d/%s",
                mbNo, now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                uuid + "-" + originalFilename);
        PutObjectArgs objectArgs = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(filePath)
                .contentType(file.getContentType())
                .stream(file.getInputStream(), file.getSize() - 1, 5 * 1024 * 1024)
                .build();
        minioClient.putObject(objectArgs);

        return String.format("%s/%s/%s", url, bucketName, filePath);
    }
}
