package site.caboomlog.backendservice.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.caboomlog.backendservice.blog.dto.TeamBlogMemberResponse;
import site.caboomlog.backendservice.post.dto.PostFlatProjection;
import site.caboomlog.backendservice.post.dto.PostResponse;
import site.caboomlog.backendservice.post.repository.PostRepository;
import site.caboomlog.backendservice.post.repository.PostRepositoryImpl;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PublicPostService {
    private final PostRepositoryImpl postRepositoryCustom;
    private final PostRepository postRepository;

    /**
     * 특정 블로그의 전체 공개 게시글 목록을 조회합니다.
     *
     * @param blogFid 조회 대상 블로그의 식별자
     * @param pageable 페이지네이션 정보 (size, page, sort 등)
     * @return 공개 게시글 목록 Page 객체
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPublicPosts(String blogFid, Pageable pageable) {
        List<PostFlatProjection> postFlatProjections = postRepositoryCustom
                .findPublicPostsByBlogFid(Optional.of(blogFid), pageable.getOffset(), pageable.getPageSize());
        List<PostResponse> postResponses = postFlatProjectionsToPostResponseList(postFlatProjections);
        long total = postRepository.countByPostPublicAndBlog_BlogFid(true, blogFid);

        return new PageImpl<>(postResponses, pageable, total);
    }

    /**
     * 전체 블로그의 모든 공개 게시글 목록을 조회합니다.
     *
     * @param pageable 페이지네이션 정보
     * @return 전체 공개 게시글 목록 Page 객체
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPublicPosts(Pageable pageable) {
        List<PostFlatProjection> postFlatProjections = postRepositoryCustom
                .findPublicPostsByBlogFid(Optional.empty(), pageable.getOffset(), pageable.getPageSize());
        List<PostResponse> postResponses = postFlatProjectionsToPostResponseList(postFlatProjections);
        long total = postRepository.countByPostPublic(true);

        return new PageImpl<>(postResponses, pageable, total);
    }

    /**
     * Flat 구조의 게시글 프로젝션 리스트를 실제 응답 DTO로 변환합니다.
     * <p>같은 게시글에 여러 카테고리가 연결된 경우 카테고리명을 병합합니다.</p>
     *
     * @param postFlatProjections 평면 구조 게시글 + 카테고리명 데이터
     * @return 변환된 게시글 응답 리스트
     */
    private List<PostResponse> postFlatProjectionsToPostResponseList(List<PostFlatProjection> postFlatProjections) {
        Map<Long, PostResponse> postMap = new LinkedHashMap<>();

        for (PostFlatProjection p : postFlatProjections) {
            postMap.computeIfAbsent(p.postId(), postId -> {
                TeamBlogMemberResponse writer = new TeamBlogMemberResponse(
                        p.mbUuid(), p.mbNickname(), p.mainBlogFid()
                );

                return new PostResponse(p.postId(), p.blogFid(), p.title(), writer, p.summary(),
                        p.thumbnail(), p.createdAt(), p.viewCount(), new ArrayList<>());
            }).getCategoryNames().add(p.categoryName());
        }
        return new ArrayList<>(postMap.values());
    }
}
