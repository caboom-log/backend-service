package site.caboomlog.backendservice.blog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.caboomlog.backendservice.blog.dto.BlogInfoResponse;
import site.caboomlog.backendservice.blog.dto.CreateBlogRequest;
import site.caboomlog.backendservice.blog.entity.Blog;
import site.caboomlog.backendservice.blog.exception.BlogFidDuplicatedException;
import site.caboomlog.backendservice.blog.exception.BlogNotFoundException;
import site.caboomlog.backendservice.blog.exception.InvalidBlogCountRangeException;
import site.caboomlog.backendservice.blog.repository.BlogRepository;
import site.caboomlog.backendservice.blogmember.BlogMemberMapping;
import site.caboomlog.backendservice.blogmember.repository.BlogMemberMappingRepository;
import site.caboomlog.backendservice.member.entity.Member;
import site.caboomlog.backendservice.member.exception.MemberNotFoundException;
import site.caboomlog.backendservice.member.repository.MemberRepository;
import site.caboomlog.backendservice.role.entity.Role;
import site.caboomlog.backendservice.role.exception.RoleNotFoundException;
import site.caboomlog.backendservice.role.repository.RoleRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;
    private final BlogMemberMappingRepository blogMemberMappingRepository;
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;

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

    @Transactional
    public void createBlog(CreateBlogRequest request, Long mbNo) {
        Optional<Member> optionalMember = memberRepository.findById(mbNo);
        if (optionalMember.isEmpty()) {
            throw new MemberNotFoundException(String.format("존재하지 않는 mbNo 입니다: %d", mbNo));
        }
        Optional<Role> optionalRole = roleRepository.findById("ROLE_OWNER");
        if (optionalRole.isEmpty()) {
            throw new RoleNotFoundException("권한이 존재하지 않습니다: ROLE_OWNER");
        }

        validateDuplicateBlogFid(request.getBlogFid());
        validateBlogCountRange(mbNo);

        Blog newBlog = Blog.ofNewBlog(
                request.getBlogFid(),
                false,
                request.getBlogName(),
                request.getBlogDesc(),
                request.isBlogPublic()
        );
        if(!hasMainBlog(mbNo)) {
            newBlog.setBlogMain();
        }
        blogRepository.save(newBlog);

        BlogMemberMapping blogMemberMapping = BlogMemberMapping.ofNewBlogMemberMapping(
                newBlog,
                optionalMember.get(),
                optionalRole.get(),
                request.getMbNickname()
        );
        blogMemberMappingRepository.save(blogMemberMapping);
    }

    private void validateDuplicateBlogFid(String blogFid) {
        if (blogRepository.existsById(blogFid)) {
            throw new BlogFidDuplicatedException(String.format("이미 사용 중인 blog fid 입니다: %s", blogFid));
        }
    }

    private void validateBlogCountRange(Long mbNo) {
        int mbBlogCount = blogMemberMappingRepository.countByMember_MbNo(mbNo);
        if (mbBlogCount >= 3) {
            throw new InvalidBlogCountRangeException("블로그는 최대 3개까지 생성 가능합니다.");
        }
    }

    private boolean hasMainBlog(Long mbNo) {
        return blogMemberMappingRepository.existsByMember_MbNoAndBlogBlogMain(mbNo, true);
    }
}
