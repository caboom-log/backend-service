package site.caboomlog.backendservice.blogmember.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import site.caboomlog.backendservice.blog.dto.TeamBlogMemberResponse;
import site.caboomlog.backendservice.blog.entity.QBlog;
import site.caboomlog.backendservice.blogmember.QBlogMemberMapping;
import site.caboomlog.backendservice.member.entity.QMember;

import java.util.List;

@RequiredArgsConstructor
public class BlogMemberMappingRepositoryImpl implements BlogMemberMappingRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<TeamBlogMemberResponse> findTeamBlogMemberInfo(String blogFid, String roleId, Pageable pageable) {
        QBlogMemberMapping mp = QBlogMemberMapping.blogMemberMapping;
        QBlog b = QBlog.blog;
        QMember m = QMember.member;

        List<TeamBlogMemberResponse> content = queryFactory
                .select(Projections.constructor(TeamBlogMemberResponse.class,
                        m.mbUuid,
                        mp.mbNickname,
                        b.blogFid))
                .from(mp)
                .join(mp.member, m)
                .join(mp.blog, b)
                .where(
                        mp.role.roleId.eq(roleId),
                        mp.blog.blogFid.eq(blogFid),
                        b.blogMain.isTrue()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(mp.mbNickname.asc())
                .fetch();
        Long total = queryFactory
                .select(mp.count())
                .from(mp)
                .join(mp.blog, b)
                .where(
                        mp.role.roleId.eq(roleId),
                        mp.blog.blogFid.eq(blogFid),
                        b.blogMain.isTrue()
                )
                .fetchOne();
        return new PageImpl<>(content, pageable, total);
    }
}