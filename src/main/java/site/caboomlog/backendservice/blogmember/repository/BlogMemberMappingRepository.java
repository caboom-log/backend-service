package site.caboomlog.backendservice.blogmember.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import site.caboomlog.backendservice.blogmember.entity.BlogMemberMapping;

import java.util.List;

public interface BlogMemberMappingRepository extends JpaRepository<BlogMemberMapping, Long>, BlogMemberMappingRepositoryCustom {
    @Query("SELECT m FROM BlogMemberMapping m JOIN FETCH m.blog WHERE m.member.mbNo = ?1 AND m.blog.blogMain = ?2")
    BlogMemberMapping findByMember_MbNoAndBlog_BlogMain(Long mbNo, boolean isMain);

    boolean existsByMember_MbNoAndBlog_BlogFid(Long mbNo, String blogFid);

    @Query("SELECT m FROM BlogMemberMapping m JOIN FETCH m.blog WHERE m.member.mbNo = ?1 AND m.blog.blogFid = ?2")
    BlogMemberMapping findByMember_MbNoAndBlog_BlogFid(Long mbNo, String blogFid);

    BlogMemberMapping findByMember_MbUuidAndBlog_BlogFid(String mbUuid, String blogFid);

    int countByMember_MbNo(Long mbNo);

    boolean existsByMember_MbNoAndBlogBlogMain(Long mbNo, boolean isMain);

    List<BlogMemberMapping> findAllByBlog_BlogFidAndRole_RoleId(String blogFid, String roleId);

    boolean existsByBlog_BlogFidAndMbNickname(String blogFid, String mbNickname);

    BlogMemberMapping findByBlog_BlogFidAndMbNickname(String blogFid, String mbNickname);

    List<BlogMemberMapping> findAllByMember_MbNoAndRole_RoleId(Long mbNo, String roleId);
}
