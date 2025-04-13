package site.caboomlog.backendservice.blogmember.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.caboomlog.backendservice.blogmember.repository.BlogMemberMappingRepository;

@Service
@RequiredArgsConstructor
public class BlogMemberMappingService {
    private final BlogMemberMappingRepository blogMemberMappingRepository;

    /**
     * 주어진 멤버 번호와 블로그 FID를 기준으로,
     * 사용자가 해당 블로그의 멤버인지 여부를 확인합니다.
     *
     * @param mbNo 사용자의 멤버 번호
     * @param blodFid 블로그의 고유 식별자 (FID)
     * @return 멤버인지 여부를 반환
     */
    public boolean isMemberOfBlog(Long mbNo, String blodFid) {
        return blogMemberMappingRepository
                .existsByMember_MbNoAndBlog_BlogFid(mbNo, blodFid);
    }
}
