package site.caboomlog.backendservice.blogmember.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.backendservice.blogmember.BlogMemberMapping;

import java.util.List;

public interface BlogMemberMappingRepository extends JpaRepository<BlogMemberMapping, Long>, BlogMemberMappingRepositoryCustom {
    BlogMemberMapping findByMember_MbNoAndBlog_BlogMain(Long mbNo, boolean isMain);

    boolean existsByMember_MbNoAndBlog_BlogFid(Long mbNo, String blogFid);

    BlogMemberMapping findByMember_MbNoAndBlog_BlogFid(Long mbNo, String blogFid);

    int countByMember_MbNo(Long mbNo);

    boolean existsByMember_MbNoAndBlogBlogMain(Long mbNo, boolean isMain);

    List<BlogMemberMapping> findAllByBlog_BlogFidAndRole_RoleId(String blogFid, String roleId);
}
