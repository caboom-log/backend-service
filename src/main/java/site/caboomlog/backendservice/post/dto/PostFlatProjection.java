package site.caboomlog.backendservice.post.dto;

import java.time.LocalDateTime;

public record PostFlatProjection(
        Long postId,
        String blogFid,
        String title,
        String summary,
        String thumbnail,
        LocalDateTime createdAt,
        Long viewCount,
        String topicName
) {
}
