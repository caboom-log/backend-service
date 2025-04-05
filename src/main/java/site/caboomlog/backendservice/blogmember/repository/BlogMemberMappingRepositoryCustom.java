package site.caboomlog.backendservice.blogmember.repository;

import site.caboomlog.backendservice.blog.dto.TeamBlogMemberResponse;
import java.util.List;

public interface BlogMemberMappingRepositoryCustom {
    List<TeamBlogMemberResponse> findTeamBlogMemberInfo(Long ownerMbNo, String blogFid, String roleId);
}
