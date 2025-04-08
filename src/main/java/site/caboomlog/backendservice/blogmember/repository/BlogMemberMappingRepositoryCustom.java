package site.caboomlog.backendservice.blogmember.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import site.caboomlog.backendservice.blog.dto.TeamBlogMemberResponse;

public interface BlogMemberMappingRepositoryCustom {
    Page<TeamBlogMemberResponse> findTeamBlogMemberInfo(String blogFid, String roleId, Pageable pageable);
}
