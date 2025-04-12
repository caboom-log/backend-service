package site.caboomlog.backendservice.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.caboomlog.backendservice.blog.entity.Blog;
import site.caboomlog.backendservice.blogmember.entity.BlogMemberMapping;
import site.caboomlog.backendservice.blogmember.repository.BlogMemberMappingRepository;
import site.caboomlog.backendservice.category.dto.CategoryResponse;
import site.caboomlog.backendservice.category.dto.CreateCategoryRequest;
import site.caboomlog.backendservice.category.entity.Category;
import site.caboomlog.backendservice.category.exception.CategoryNotFoundException;
import site.caboomlog.backendservice.category.repository.CategoryRepository;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.common.exception.UnauthenticatedException;
import site.caboomlog.backendservice.topic.entity.Topic;
import site.caboomlog.backendservice.topic.exception.TopicNotFoundException;
import site.caboomlog.backendservice.topic.repository.TopicRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final BlogMemberMappingRepository blogMemberMappingRepository;
    private final TopicRepository topicRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 블로그에 새로운 카테고리를 생성합니다.
     * <p>
     * 블로그 소유자만 카테고리를 등록할 수 있으며,
     * 루트 카테고리 또는 최대 5단계까지의 하위 카테고리를 생성할 수 있습니다.
     * </p>
     *
     * @param blogFid 블로그 식별자
     * @param mbNo    로그인한 사용자의 회원 번호
     * @param request 카테고리 생성 요청 정보
     * @throws UnauthenticatedException   사용자가 블로그 소유자가 아닐 경우
     * @throws TopicNotFoundException     요청한 토픽이 존재하지 않을 경우
     * @throws CategoryNotFoundException  상위 카테고리가 존재하지 않을 경우
     * @throws BadRequestException        상위 카테고리 depth 초과 또는 비공개 하위 등록 시
     */
    @Transactional
    public void createCategory(String blogFid, Long mbNo, CreateCategoryRequest request) {

        BlogMemberMapping ownerMapping = blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(mbNo, blogFid);
        if (!"ROLE_OWNER".equalsIgnoreCase(ownerMapping.getRole().getRoleId())) {
            throw new UnauthenticatedException("블로그 소유자가 아닙니다.");
        }
        Blog blog = ownerMapping.getBlog();

        Optional<Topic> optionalTopic = topicRepository.findById(request.getTopicId());
        if (optionalTopic.isEmpty()) {
            throw new TopicNotFoundException("토픽을 찾을 수 없습니다.");
        }
        Topic topic = optionalTopic.get();

        long newCategoryOrder = 0;
        long depth = 1;
        Category parent = null;

        if (request.getCategoryPid() != null) {
            parent = categoryRepository.findById(request.getCategoryPid())
                    .orElseThrow(()->new CategoryNotFoundException("상위 카테고리가 존재하지 않습니다."));
            if (parent.getDepth() >= 5) {
                throw new BadRequestException("카테고리 depth는 최대 5까지만 가능합니다.");
            }
            if (Boolean.FALSE.equals(parent.getCategoryPublic()) && request.isCategoryPublic()) {
                throw new BadRequestException("상위 카테고리가 비공개 카테고리입니다. 비공개 카테고리 하위에 공개 카테고리를 등록할 수 없습니다.");
            }
            newCategoryOrder = parent.getChildCategories().size() + 1;
            depth += parent.getDepth();
        } else {
            newCategoryOrder = categoryRepository.countByBlog_BlogFidAndParentCategory(blogFid, null) + 1;
        }

        Category newCategory = Category.ofNewCategory(blog, parent, topic,
                request.getCategoryName(), request.isCategoryPublic(), newCategoryOrder,
                depth);
        categoryRepository.save(newCategory);
    }

    /**
     * 블로그의 모든 카테고리를 트리 형태로 조회합니다.
     * <p>
     * 블로그 소유자만 조회할 수 있으며, 루트 카테고리부터 하위 카테고리까지 포함됩니다.
     * </p>
     *
     * @param blogFid 블로그 식별자
     * @param mbNo    로그인한 사용자의 회원 번호
     * @return 트리 구조의 전체 카테고리 목록
     * @throws UnauthenticatedException 사용자가 블로그 소유자가 아닐 경우
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(String blogFid, Long mbNo) {
        BlogMemberMapping ownerMapping = blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(mbNo, blogFid);
        if (!"ROLE_OWNER".equalsIgnoreCase(ownerMapping.getRole().getRoleId())) {
            throw new UnauthenticatedException("블로그 소유자가 아닙니다.");
        }

        List<Category> categories = categoryRepository.findAllByBlog_BlogFid(blogFid);

        return buildCategoryTree(categories);
    }

    /**
     * 블로그의 공개된 카테고리만 트리 구조로 조회합니다.
     * <p>
     * 인증 없이 누구나 접근 가능한 API를 위한 메서드입니다.
     * </p>
     *
     * @param blogFid 블로그 식별자
     * @return 트리 구조의 공개된 카테고리 목록
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllPublicCategories(String blogFid) {

        List<Category> categories = categoryRepository.findAllPublicByBlog_BlogFid(blogFid);

        return buildCategoryTree(categories);
    }

    /**
     * 카테고리의 공개 여부를 변경합니다.
     * <p>
     * 블로그 소유자만 변경할 수 있으며,
     * 비공개로 변경할 경우 해당 카테고리 및 모든 하위 카테고리들이 함께 비공개로 전환됩니다.
     * 공개로 변경할 경우에는 해당 카테고리만 공개로 전환됩니다.
     * </p>
     *
     * @param mbNo        로그인한 사용자(블로그 소유자)의 회원 번호
     * @param blogFid     블로그 식별자
     * @param categoryId  공개 여부를 변경할 카테고리 ID
     * @param blogPublic  true: 공개, false: 비공개
     * @throws UnauthenticatedException 사용자가 블로그 소유자가 아닐 경우
     * @throws CategoryNotFoundException 카테고리가 존재하지 않을 경우
     * @throws BadRequestException 카테고리가 해당 블로그에 속하지 않은 경우
     */
    @Transactional
    public void changeVisibility(Long mbNo, String blogFid, Long categoryId, boolean blogPublic) {
        BlogMemberMapping ownerMapping = blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(mbNo, blogFid);
        if (!"ROLE_OWNER".equalsIgnoreCase(ownerMapping.getRole().getRoleId())) {
            throw new UnauthenticatedException("블로그 소유자가 아닙니다.");
        }
        Category category = categoryRepository.findByCategoryId(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("카테고리가 존재하지 않습니다."));
        if (!blogFid.equals(category.getBlog().getBlogFid())) {
            throw new BadRequestException("카테고리가 해당 블로그 소속이 아닙니다.");
        }
        if (blogPublic) {
            category.changeVisibility(blogPublic);
            categoryRepository.save(category);
        } else {
            List<Category> categories = categoryRepository.findAllByBlog_BlogFid(blogFid);
            List<Category> children = collectChildren(category, categories);
            for (Category c : children) {
                c.changeVisibility(blogPublic);
                categoryRepository.save(c);
            }
        }
    }

    /**
     * 지정된 카테고리와 그 모든 하위 카테고리를 BFS 방식으로 수집합니다.
     *
     * @param category       시작 카테고리 (루트 또는 특정 상위 카테고리)
     * @param allCategories  전체 카테고리 목록
     * @return 시작 카테고리를 포함한 모든 하위 카테고리 리스트
     */
    private List<Category> collectChildren(Category category, List<Category> allCategories) {
        List<Category> children = new ArrayList<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(category.getCategoryId());
        children.add(category);

        while (!queue.isEmpty()) {
            Long currentId = queue.poll();
            for (Category c : allCategories) {
                if (c.getParentCategory() != null &&
                        Objects.equals(c.getParentCategory().getCategoryId(), currentId)) {
                    queue.add(c.getCategoryId());
                    children.add(c);
                }
            }
        }
        return children;
    }


    /**
     * 카테고리 목록을 트리 구조로 변환합니다.
     * <p>
     * 부모-자식 관계를 기반으로 트리를 구성하며,
     * 부모가 없는 루트 카테고리부터 하위 카테고리를 연결합니다.
     * </p>
     *
     * @param categories 평면 구조의 카테고리 목록
     * @return 트리 구조로 구성된 카테고리 응답 목록
     */
    private List<CategoryResponse> buildCategoryTree(List<Category> categories) {
        Map<Long, CategoryResponse> map = new LinkedHashMap<>();
        List<CategoryResponse> rootList = new ArrayList<>();

        for (Category category : categories) {
            CategoryResponse dto = new CategoryResponse(category);
            map.put(category.getCategoryId(), dto);
        }

        for (Category category : categories) {
            CategoryResponse current = map.get(category.getCategoryId());
            if (category.getParentCategory() != null) {
                CategoryResponse parent = map.get(category.getParentCategory().getCategoryId());
                parent.getChildren().add(current);
            } else {
                rootList.add(current);
            }
        }
        return rootList;
    }

}
