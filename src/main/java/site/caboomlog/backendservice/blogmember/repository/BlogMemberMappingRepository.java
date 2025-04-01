package site.caboomlog.backendservice.blogmember.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.backendservice.blogmember.BlogMemberMapping;

public interface BlogMemberMappingRepository extends JpaRepository<BlogMemberMapping, Long> {
    BlogMemberMapping findByMember_MbNoAndBlog_BlogMain(Long mbNo, boolean isMain);
    boolean existsByMember_MbNoAndBlog_BlogFid(Long mbNo, String blogFid);
}
