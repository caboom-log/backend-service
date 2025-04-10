package site.caboomlog.backendservice.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.caboomlog.backendservice.blog.entity.Blog;
import site.caboomlog.backendservice.blogmember.entity.BlogMemberMapping;
import site.caboomlog.backendservice.blogmember.repository.BlogMemberMappingRepository;
import site.caboomlog.backendservice.category.dto.CreateCategoryRequest;
import site.caboomlog.backendservice.category.entity.Category;
import site.caboomlog.backendservice.category.repository.CategoryRepository;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.common.exception.UnauthenticatedException;
import site.caboomlog.backendservice.topic.entity.Topic;
import site.caboomlog.backendservice.topic.exception.TopicNotFoundException;
import site.caboomlog.backendservice.topic.repository.TopicRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final BlogMemberMappingRepository blogMemberMappingRepository;
    private final TopicRepository topicRepository;
    private final CategoryRepository categoryRepository;

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

        Category parent = null;
        long newCategoryOrder = 0;
        long depth = 1;
        Optional<Category> optionalParent = categoryRepository.findById(request.getCategoryPid());
        if (optionalParent.isPresent()) {
            parent = optionalParent.get();
            if (parent.getDepth() >= 5) {
                throw new BadRequestException("카테고리 depth는 최대 5까지만 가능합니다.");
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


}
