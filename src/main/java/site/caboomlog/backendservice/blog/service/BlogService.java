package site.caboomlog.backendservice.blog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.caboomlog.backendservice.blog.dto.BlogInfoResponse;
import site.caboomlog.backendservice.blog.dto.CreateBlogRequest;
import site.caboomlog.backendservice.blog.dto.ModifyBlogInfoRequest;
import site.caboomlog.backendservice.blog.entity.Blog;
import site.caboomlog.backendservice.blog.entity.BlogType;
import site.caboomlog.backendservice.blog.exception.BlogFidDuplicatedException;
import site.caboomlog.backendservice.blog.exception.BlogNotFoundException;
import site.caboomlog.backendservice.blog.exception.InvalidBlogCountRangeException;
import site.caboomlog.backendservice.blog.repository.BlogRepository;
import site.caboomlog.backendservice.blogmember.BlogMemberMapping;
import site.caboomlog.backendservice.blogmember.repository.BlogMemberMappingRepository;
import site.caboomlog.backendservice.common.exception.BadRequestException;
import site.caboomlog.backendservice.common.exception.UnauthenticatedException;
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
    @Transactional(readOnly = true)
    public BlogInfoResponse getBlogInfo(String blogFid) {
        Optional<Blog> optionalBlog = blogRepository.findByBlogFid(blogFid);
        if (optionalBlog.isEmpty()) {
            throw new BlogNotFoundException("존재하지 않는 블로그입니다.");
        }
        return new BlogInfoResponse(
                optionalBlog.get().getBlogName(),
                optionalBlog.get().getBlogDescription(),
                optionalBlog.get().getBlogMainImg()
        );
    }

    /**
     * 블로그를 생성합니다.
     *
     * <p>회원과 권한이 유효한지 검증하며, 블로그 FID 중복 여부와 블로그 수 제한도 확인합니다.
     * 첫 블로그일 경우 자동으로 메인 블로그로 설정됩니다.</p>
     *
     * @param request 블로그 생성 요청
     * @param mbNo 회원 번호
     * @throws MemberNotFoundException 회원이 존재하지 않는 경우
     * @throws RoleNotFoundException ROLE_OWNER 권한이 존재하지 않는 경우
     * @throws BlogFidDuplicatedException 중복된 blogFid인 경우
     * @throws InvalidBlogCountRangeException 블로그 수가 최대치를 초과한 경우
     */
    @Transactional
    public void createBlog(CreateBlogRequest request, Long mbNo) {
        Optional<Member> optionalMember = memberRepository.findById(mbNo);
        if (optionalMember.isEmpty()) {
            throw new MemberNotFoundException(String.format("존재하지 않는 사용자입니다."));
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
                request.isBlogPublic(),
                BlogType.valueOf(request.getBlogType().toUpperCase())
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

    /**
     * 블로그 정보를 수정합니다.
     *
     * <p>요청한 사용자가 해당 블로그의 소유자인지 검증한 후, 블로그 이름/설명/공개 여부를 수정합니다.</p>
     *
     * @param blogFid 수정할 블로그의 식별자
     * @param mbNo 회원 번호
     * @param request 수정 요청 정보
     * @throws BlogNotFoundException 블로그가 존재하지 않는 경우
     * @throws BadRequestException 블로그 소유자가 아닌 경우
     */
    @Transactional
    public void modifyBlogInfo(String blogFid, Long mbNo, ModifyBlogInfoRequest request) {
        verifyMemberIsBlogOwner(mbNo, blogFid);
        Optional<Blog> optionalBlog = blogRepository.findByBlogFid(blogFid);
        if (optionalBlog.isEmpty()) {
            throw new BlogNotFoundException(String.format("블로그가 존재하지 않습니다. blogFid: %s", blogFid));
        }
        Blog blog = optionalBlog.get();
        blog.modifyBlogInfo(request.getBlogName(), request.getBlogDesc(), request.isBlogPublic());
        blogRepository.save(blog);
    }

    /**
     * 메인 블로그를 변경합니다.
     *
     * <p>기존 메인 블로그를 찾아 해제하고, 지정된 블로그를 새로운 메인 블로그로 설정합니다.
     * 요청자는 반드시 새 블로그의 소유자여야 합니다.</p>
     *
     * @param blogFid 새로 메인으로 설정할 블로그 식별자
     * @param mbNo 회원 번호
     * @throws BadRequestException 기존 메인 블로그가 없거나, 권한이 없는 경우
     */
    @Transactional
    public void switchMainBlogTo(String blogFid, Long mbNo) {
        Blog newMain = verifyBlogOwnerAndGetNewMain(blogFid, mbNo);
        Blog oldMain = validateMainBlogExistsAndGetOldMain(mbNo);
        Blog.changeMainBlog(oldMain, newMain);
    }

    /**
     * 기존 메인 블로그가 존재하는지 확인하고 반환합니다.
     *
     * @param mbNo 회원 번호
     * @return 기존 메인 블로그 객체
     * @throws BadRequestException 메인 블로그가 존재하지 않는 경우
     */
    private Blog validateMainBlogExistsAndGetOldMain(Long mbNo) {
        BlogMemberMapping oldBlogMapping = blogMemberMappingRepository
                .findByMember_MbNoAndBlog_BlogMain(mbNo, true);
        if (oldBlogMapping == null) {
            throw new BadRequestException("메인 블로그는 반드시 하나 존재해야 합니다.");
        }
        return oldBlogMapping.getBlog();
    }

    /**
     * 지정된 블로그의 소유자인지 확인하고, 블로그 객체를 반환합니다.
     *
     * @param blogFid 블로그 식별자
     * @param mbNo 회원 번호
     * @return 블로그 객체
     * @throws BadRequestException 블로그 소유자가 아닌 경우
     */
    private Blog verifyBlogOwnerAndGetNewMain(String blogFid, Long mbNo) {
        BlogMemberMapping newBlogMapping = blogMemberMappingRepository
                .findByMember_MbNoAndBlog_BlogFid(mbNo, blogFid);
        if (newBlogMapping == null || !newBlogMapping.getRole().getRoleId().equalsIgnoreCase("ROLE_OWNER")) {
            throw new UnauthenticatedException("블로그 소유자가 아닙니다.");
        }
        return newBlogMapping.getBlog();
    }

    /**
     * 지정된 블로그의 소유자인지 확인합니다.
     *
     * @param mbNo 회원 번호
     * @param blogFid 블로그 식별자
     * @throws BadRequestException 블로그 소유자가 아닌 경우
     */
    private void verifyMemberIsBlogOwner(Long mbNo, String blogFid) {
        BlogMemberMapping mapping = blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(mbNo, blogFid);
        if (!mapping.getRole().getRoleId().equalsIgnoreCase("ROLE_OWNER")) {
            throw new UnauthenticatedException("블로그 소유자가 아닙니다.");
        }
    }

    /**
     * 블로그 FID 중복 여부를 검증합니다.
     *
     * @param blogFid 블로그 식별자
     * @throws BlogFidDuplicatedException 중복된 blogFid인 경우
     */
    private void validateDuplicateBlogFid(String blogFid) {
        if (blogRepository.existsByBlogFid(blogFid)) {
            throw new BlogFidDuplicatedException(String.format("이미 사용 중인 blog fid 입니다: %s", blogFid));
        }
    }

    /**
     * 해당 회원의 블로그 수가 제한 범위를 초과하지 않는지 검증합니다.
     *
     * @param mbNo 회원 번호
     * @throws InvalidBlogCountRangeException 블로그 수가 최대치를 초과한 경우
     */
    private void validateBlogCountRange(Long mbNo) {
        int mbBlogCount = blogMemberMappingRepository.countByMember_MbNo(mbNo);
        if (mbBlogCount >= 3) {
            throw new InvalidBlogCountRangeException("블로그는 최대 3개까지 생성 가능합니다.");
        }
    }

    /**
     * 해당 회원이 메인 블로그를 하나 가지고 있는지 확인합니다.
     *
     * @param mbNo 회원 번호
     * @return 메인 블로그 존재 여부
     */
    private boolean hasMainBlog(Long mbNo) {
        return blogMemberMappingRepository.existsByMember_MbNoAndBlogBlogMain(mbNo, true);
    }
}
