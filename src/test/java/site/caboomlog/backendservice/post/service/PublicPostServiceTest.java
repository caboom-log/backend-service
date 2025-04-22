package site.caboomlog.backendservice.post.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import site.caboomlog.backendservice.post.dto.PostFlatProjection;
import site.caboomlog.backendservice.post.dto.PostResponse;
import site.caboomlog.backendservice.post.repository.PostRepository;
import site.caboomlog.backendservice.post.repository.PostRepositoryImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class PublicPostServiceTest {
    @Mock
    PostRepositoryImpl postRepositoryCustom;
    @Mock
    PostRepository postRepository;
    @InjectMocks
    PublicPostService publicPostService;

    @Test
    @DisplayName("특정 블로그 전체 게시글 조회")
    void getAllPublicPosts() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<PostFlatProjection> postFlatProjections = List.of(
                new PostFlatProjection(
                        1L,
                        "caboom",
                        "제목1",
                        UUID.randomUUID().toString(),
                        "작성자",
                        "caboom",
                        "안녕하세요",
                        "img1.png",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        100L,
                        "공지사항"));
        Mockito.when(postRepositoryCustom
                        .findPublicPostsByBlogFid(any(), anyLong(), anyInt()))
                .thenReturn(postFlatProjections);
        Mockito.when(postRepository.countByPostPublicAndBlog_BlogFid(anyBoolean(), anyString()))
                .thenReturn(1);

        // when
        Page<PostResponse> result = publicPostService.getAllPublicPosts("caboom", pageable);

        // then
        Assertions.assertEquals(1, result.getContent().size());
        Assertions.assertEquals("제목1", result.getContent().get(0).getTitle());
        Assertions.assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("전체 블로그의 공개 게시글 전체 조회")
    void getAllPublicPostsWithoutBlogFid() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        List<PostFlatProjection> postFlatProjections = List.of(
                new PostFlatProjection(
                        1L,
                        "caboom",
                        "제목2",
                        UUID.randomUUID().toString(),
                        "작성자",
                        "caboom",
                        "안녕하세요",
                        "img1.png",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        100L,
                        "공지사항"));

        Mockito.when(postRepositoryCustom
                        .findPublicPostsByBlogFid(eq(Optional.empty()), anyLong(), anyInt()))
                .thenReturn(postFlatProjections);
        Mockito.when(postRepository.countByPostPublic(anyBoolean()))
                .thenReturn(1);

        // when
        Page<PostResponse> result = publicPostService.getAllPublicPosts(pageable);

        // then
        Assertions.assertEquals(1, result.getContent().size());
        Assertions.assertEquals("제목2", result.getContent().get(0).getTitle());
        Assertions.assertEquals(1L, result.getTotalElements());
    }
}
