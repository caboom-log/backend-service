package site.caboomlog.backendservice.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.caboomlog.backendservice.blogmember.entity.BlogMemberMapping;
import site.caboomlog.backendservice.blogmember.repository.BlogMemberMappingRepository;
import site.caboomlog.backendservice.category.entity.Category;
import site.caboomlog.backendservice.category.exception.CategoryNotFoundException;
import site.caboomlog.backendservice.category.repository.CategoryRepository;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.common.exception.DatabaseException;
import site.caboomlog.backendservice.common.exception.UnauthenticatedException;
import site.caboomlog.backendservice.post.dto.CreatePostRequest;
import site.caboomlog.backendservice.post.dto.PostDetailResponse;
import site.caboomlog.backendservice.post.entity.Post;
import site.caboomlog.backendservice.post.entity.PostCategoryMapping;
import site.caboomlog.backendservice.post.exception.PostNotFoundException;
import site.caboomlog.backendservice.post.repository.PostCategoryMappingRepository;
import site.caboomlog.backendservice.post.repository.PostRepository;
import site.caboomlog.backendservice.post.repository.PostRepositoryImpl;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogPostService {
    private final PostRepository postRepository;
    private final BlogMemberMappingRepository blogMemberMappingRepository;
    private final CategoryRepository categoryRepository;
    private final PostCategoryMappingRepository postCategoryMappingRepository;
    private final PostRepositoryImpl postRepositoryCustom;

    /**
     * 게시글을 생성합니다.
     * <p>블로그 소유자 또는 멤버만 게시글을 생성할 수 있습니다.</p>
     * <p>카테고리가 지정되지 않은 경우 "카테고리 없음"으로 자동 설정됩니다.</p>
     *
     * @param blogFid 블로그 식별자
     * @param mbNo 작성자 사용자 식별자
     * @param request 게시글 생성 요청 DTO
     * @throws UnauthenticatedException 사용자가 블로그 멤버가 아닌 경우
     * @throws BadRequestException 제목이 없거나 카테고리 유효성 실패
     */
    @Transactional
    public void createPost(String blogFid, Long mbNo, CreatePostRequest request) {
        BlogMemberMapping ownerMapping = blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(mbNo, blogFid);
        if (ownerMapping == null ||
                !("ROLE_OWNER".equalsIgnoreCase(ownerMapping.getRole().getRoleId()) ||
                "ROLE_MEMBER".equalsIgnoreCase(ownerMapping.getRole().getRoleId()))) {
            throw new UnauthenticatedException("포스팅은 블로그 회원만 작성할 수 있습니다.");
        }

        if (request.getCategoryIds().isEmpty()) {
            Category categoryNone = categoryRepository
                    .findByBlog_BlogFidAndAndCategoryName(blogFid, "카테고리 없음")
                            .orElseThrow(() -> new DatabaseException("카테고리 없음 카테고리가 없음!"));
            request.setCategoryIds(List.of(categoryNone.getCategoryId()));
        }

        Post post = Post.ofNewPost(ownerMapping.getBlog(), ownerMapping.getMember(),
                request.getTitle(), request.getContent(), request.isPostPublic(), request.getThumbnail());
        postRepository.save(post);

        for (Long categoryId : request.getCategoryIds()) {
            Category category = categoryRepository
                    .findByCategoryId(categoryId)
                    .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다."));
            if (!blogFid.equals(category.getBlog().getBlogFid())) {
                throw new BadRequestException("해당 카테고리가 블로그 소속이 아닙니다.");
            }
            if ((Boolean.FALSE.equals(category.getCategoryPublic())
                    || Boolean.FALSE.equals(ownerMapping.getBlog().getBlogPublic()))
                    && request.isPostPublic()) {
                throw new BadRequestException("비공개 블로그/카테고리에는 공개 포스팅을 작성할 수 없습니다.");
            }

            PostCategoryMapping postCategoryMapping = PostCategoryMapping.ofNewPostCategoryMapping(category, post);
            postCategoryMappingRepository.save(postCategoryMapping);
        }
    }

    /**
     * 게시글 상세 정보를 조회합니다.
     * <p>비공개 게시글은 작성자 본인만 접근 가능합니다.</p>
     *
     * @param blogFid 블로그 식별자
     * @param postId 게시글 식별자
     * @param mbNo (선택) 로그인된 사용자 식별자
     * @return 게시글 상세 응답 DTO
     * @throws UnauthenticatedException 비공개 게시글에 접근 권한이 없을 경우
     * @throws BadRequestException 블로그 FID가 일치하지 않는 경우
     * @throws PostNotFoundException 게시글이 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(String blogFid, Long postId, Long mbNo) {
        Post post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글이 존재하지 않습니다."));
        if (!post.isPostPublic()) {
            if (mbNo == null || !mbNo.equals(post.getWriter().getMbNo())) {
                throw new UnauthenticatedException("게시글을 읽을 수 있는 권한이 없습니다.");
            }
        }
        if (!blogFid.equals(post.getBlog().getBlogFid())) {
            throw new BadRequestException("잘못된 요청입니다.");
        }

        return postRepositoryCustom.findPostDetailById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글이 존재하지 않습니다."));
    }
}
