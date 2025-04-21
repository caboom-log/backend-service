package site.caboomlog.backendservice.common.image.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.caboomlog.backendservice.common.image.dto.ImageDto;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService implements ImageUploadService {
    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.bucket}")
    private String bucketName;

    private final MinioClient minioClient;

    @Override
    public ImageDto uploadFile(Long mbNo, MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        originalFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8);

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

        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
        long size = file.getSize();
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        String imageUrl = String.format("%s/%s/%s", minioUrl, bucketName, filePath);
        return new ImageDto(originalFilename, size, width, height, imageUrl, null);
    }

    public void deleteFile(String url) {
        String objectName = extractObjectName(url);

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("파일 삭제 실패: {}", objectName, e);
        }
    }

    private String extractObjectName(String fileUrl) {
        return fileUrl.substring(fileUrl.indexOf(bucketName) + bucketName.length() + 1);
    }

}
