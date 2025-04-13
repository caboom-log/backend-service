package site.caboomlog.backendservice.post.repository;

import site.caboomlog.backendservice.post.dto.PostDetailResponse;
import site.caboomlog.backendservice.post.dto.PostFlatProjection;

import java.util.List;
import java.util.Optional;

public interface PostRepositoryCustom {
    List<PostFlatProjection> findPublicPostsByBlogFid(Optional<String> blogFid, long offset, int limit);

    Optional<PostDetailResponse> findPostDetailById(Long postId);
}
