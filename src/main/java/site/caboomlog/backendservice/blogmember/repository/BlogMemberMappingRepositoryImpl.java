package site.caboomlog.backendservice.blogmember.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import site.caboomlog.backendservice.blog.dto.TeamBlogMemberResponse;
import site.caboomlog.backendservice.blog.entity.QBlog;
import site.caboomlog.backendservice.blogmember.QBlogMemberMapping;

import java.util.List;

@RequiredArgsConstructor
public class BlogMemberMappingRepositoryImpl implements BlogMemberMappingRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<TeamBlogMemberResponse> findTeamBlogMemberInfo(Long ownerMbNo, String blogFid, String roleId) {
        QBlogMemberMapping mp = QBlogMemberMapping.blogMemberMapping;
        QBlog b = QBlog.blog;

        return queryFactory
                .select(Projections.constructor(TeamBlogMemberResponse.class,
                        mp.member.mbNo,
                        mp.mbNickname,
                        b.blogFid))
                .from(mp)
                .join(mp.blog, b)
                .where(
                        mp.member.mbNo.eq(ownerMbNo),
                        mp.role.roleId.eq(roleId),
                        mp.blog.blogFid.eq(blogFid),
                        b.blogMain.isTrue()
                )
                .fetch();
    }
}

/*
select mp.mb_no, mp.mb_nickname, b.blog_fid
from blog_member_mappings mp
inner join blogs b on  mp.blog_id = b.blog_id
where mp.mb_no=ownerMbNo
and mp.blog_id=blogFid
and mp.role_id=roleId
and b.blog_main = 1;
 */