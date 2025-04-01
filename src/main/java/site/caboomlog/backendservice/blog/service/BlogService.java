package site.caboomlog.backendservice.blog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.caboomlog.backendservice.blog.dto.BlogInfoResponse;
import site.caboomlog.backendservice.blog.entity.Blog;
import site.caboomlog.backendservice.blog.exception.BlogNotFoundException;
import site.caboomlog.backendservice.blog.repository.BlogRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;

    /**
     * 블로그 FID를 기준으로 블로그 정보를 조회하여 응답 DTO로 반환합니다.
     *
     * @param blogFid 블로그의 고유 식별자 (FID)
     * @return {@link BlogInfoResponse} 블로그 이름, 설명, 대표 이미지 포함
     * @throws BlogNotFoundException 해당 블로그가 존재하지 않을 경우
     */
    public BlogInfoResponse getBlogInfo(String blogFid) {
        Optional<Blog> optionalBlog = blogRepository.findByBlogFid(blogFid);
        if (optionalBlog.isEmpty()) {
            throw new BlogNotFoundException(blogFid + " not found");
        }
        return new BlogInfoResponse(
                optionalBlog.get().getBlogName(),
                optionalBlog.get().getBlogDescription(),
                optionalBlog.get().getBlogMainImg()
        );
    }

}
