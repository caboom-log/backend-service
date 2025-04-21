package site.caboomlog.backendservice.common.image.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Profile;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import site.caboomlog.backendservice.common.image.dto.ImageDto;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URLEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Profile("local")
class MinioServiceTest {

    @InjectMocks
    private MinioService minioService;
    @Mock
    private MinioClient minioClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(minioService, "minioUrl", "http://localhost:9000");
        ReflectionTestUtils.setField(minioService, "bucketName", "test-bucket");
    }

    @Test
    @DisplayName("이미지 파일 저장")
    void uploadFile() throws Exception {
        // given
        byte[] imageData = createDummyImage();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test image.png", "image/png", imageData
        );

        // when
        ImageDto result = minioService.uploadFile(1L, file);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFilename()).contains(URLEncoder.encode(file.getOriginalFilename()));
        assertThat(result.getUrl()).contains("test-bucket/1/");
        assertThat(result.getSize()).isEqualTo((long) imageData.length);
        assertThat(result.getWidth()).isEqualTo(1);
        assertThat(result.getHeight()).isEqualTo(1);

        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("이미지 파일 삭제")
    void deleteFile() throws Exception {
        // given
        String fileUrl = "http://localhost:9000/test-bucket/1/2025/04/20/test-file.png";

        // when
        minioService.deleteFile(fileUrl);

        // then
        ArgumentCaptor<RemoveObjectArgs> captor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        verify(minioClient, times(1)).removeObject(captor.capture());

        RemoveObjectArgs capturedArgs = captor.getValue();
        assertThat(capturedArgs.object()).isEqualTo("1/2025/04/20/test-file.png");
    }

    private byte[] createDummyImage() throws Exception {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        }
    }
}