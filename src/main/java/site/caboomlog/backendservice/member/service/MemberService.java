package site.caboomlog.backendservice.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.caboomlog.backendservice.blogmember.entity.BlogMemberMapping;
import site.caboomlog.backendservice.blogmember.repository.BlogMemberMappingRepository;
import site.caboomlog.backendservice.member.dto.GetMemberResponse;
import site.caboomlog.backendservice.member.exception.MainBlogNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final BlogMemberMappingRepository blogMemberMappingRepository;

    /**
     * 주어진 회원 번호(`mbNo`)에 해당하는 메인 블로그 정보를 조회합니다.
     *
     * <p>회원과 연결된 블로그 중 `blogMain`이 true로 설정된 블로그를 조회하며,
     * 존재하지 않을 경우 {@link MainBlogNotFoundException}을 발생시킵니다.</p>
     *
     * @param mbNo 회원 번호
     * @return 회원 이메일 및 메인 블로그 FID를 포함한 {@link GetMemberResponse}
     * @throws MainBlogNotFoundException 메인 블로그가 존재하지 않을 경우
     */
    public GetMemberResponse getMemberByMbNo(Long mbNo) {
        BlogMemberMapping result = blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogMain(mbNo, true);
        if (result == null) {
            log.error("Main Blog Not Found!!!");
            throw new MainBlogNotFoundException("메인 블로그는 반드시 하나 이상 존재해야 합니다.");
        }
        return new GetMemberResponse(
                result.getMember().getMbUuid(),
                result.getMember().getMbEmail(),
                result.getBlog().getBlogFid());
    }
}
