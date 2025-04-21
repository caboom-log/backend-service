package site.caboomlog.backendservice.common.image.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @Column
    private String filename;

    @Column
    private Long size;

    @Column
    private String url;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    private Image(Long imageId, String filename, Long size, String url, LocalDateTime createdAt) {
        this.imageId = imageId;
        this.filename = filename;
        this.size = size;
        this.url = url;
        this.createdAt = createdAt;
    }

    public static Image ofNewImage(String filename, Long size, String url) {
        return new Image(null, filename, size, url, null);
    }
}
