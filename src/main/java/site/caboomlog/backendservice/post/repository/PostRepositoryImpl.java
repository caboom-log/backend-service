package site.caboomlog.backendservice.post.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import site.caboomlog.backendservice.blog.dto.TeamBlogMemberResponse;
import site.caboomlog.backendservice.blog.entity.QBlog;
import site.caboomlog.backendservice.blogmember.entity.QBlogMemberMapping;
import site.caboomlog.backendservice.category.entity.QCategory;
import site.caboomlog.backendservice.member.entity.QMember;
import site.caboomlog.backendservice.post.dto.PostDetailResponse;
import site.caboomlog.backendservice.post.dto.PostFlatProjection;
import site.caboomlog.backendservice.post.entity.QPost;
import site.caboomlog.backendservice.post.entity.QPostCategoryMapping;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    /**
     * 공개된 게시글(Post) 목록을 조회합니다.
     * <p>선택적으로 blogFid를 기준으로 필터링하며, 글쓴이의 메인 블로그 FID, 카테고리 정보도 함께 조회됩니다.</p>
     *
     * @param blogFid 블로그 FID (Optional, null일 경우 전체에서 조회)
     * @param offset 페이징을 위한 시작 위치
     * @param limit 조회할 게시글 수
     * @return 게시글 목록 (카테고리별 평탄화된 구조로 반환됨)
     */
    @Override
    public List<PostFlatProjection> findPublicPostsByBlogFid(Optional<String> blogFid, long offset, int limit) {
        QPost post = QPost.post;
        QMember member = QMember.member;
        QBlog blog = QBlog.blog;
        QBlogMemberMapping mapping = QBlogMemberMapping.blogMemberMapping;
        QPostCategoryMapping pcm = QPostCategoryMapping.postCategoryMapping;
        QCategory category = QCategory.category;

        QBlog writerMainBlog = new QBlog("writerMainBlog");
        QBlogMemberMapping writerMapping = new QBlogMemberMapping("writerMapping");
        JPQLQuery<String> mainBlogFidSub = getMainBlogFidSubquery(writerMainBlog, writerMapping, member);

        BooleanBuilder whereClause = new BooleanBuilder(post.postPublic.isTrue());
        blogFid.ifPresent(fid -> whereClause.and(blog.blogFid.eq(fid)));

        return queryFactory
                .select(Projections.constructor(PostFlatProjection.class,
                        post.postId,
                        blog.blogFid,
                        post.postTitle,
                        member.mbUuid,
                        mapping.mbNickname,
                        mainBlogFidSub,
                        new CaseBuilder()
                                .when(post.postContent.length().gt(50))
                                .then(post.postContent.substring(0, 50).concat(" ... "))
                                .otherwise(post.postContent),
                        post.thumbnail,
                        post.createdAt,
                        post.viewCount,
                        category.categoryName
                ))
                .from(post)
                    .join(post.blog, blog)
                    .join(post.writer, member)
                    .join(mapping).on(mapping.blog.eq(blog).and(mapping.member.eq(member)))
                    .leftJoin(pcm).on(pcm.post.eq(post))
                    .leftJoin(pcm.category, category)
                .where(whereClause)
                .orderBy(post.createdAt.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    /**
     * 게시글 ID를 기준으로 상세 게시글 정보를 조회합니다.
     * <p>작성자 및 수정자의 메인 블로그 FID, 닉네임, 카테고리 이름까지 포함된 응답을 구성합니다.</p>
     *
     * @param postId 조회할 게시글 ID
     * @return Optional<PostDetailResponse> (없으면 빈 Optional 반환)
     */
    @Override
    public Optional<PostDetailResponse> findPostDetailById(Long postId) {
        QPost post = QPost.post;
        QMember writer = new QMember("writer");
        QMember modifier = new QMember("modifier");
        QPostCategoryMapping pcm = QPostCategoryMapping.postCategoryMapping;
        QCategory category = QCategory.category;

        QBlogMemberMapping writerMapping = new QBlogMemberMapping("writerMapping");
        QBlogMemberMapping modifierMapping = new QBlogMemberMapping("modifierMapping");

        QBlog writerMainBlog = new QBlog("writerMainBlog");
        QBlogMemberMapping writerMainMapping = new QBlogMemberMapping("writerMainMapping");
        JPQLQuery<String> writerMainBlogFidSub = getMainBlogFidSubquery(writerMainBlog, writerMainMapping, writer);

        QBlog modifierMainBlog = new QBlog("modifierMainBlog");
        QBlogMemberMapping modifierMainMapping = new QBlogMemberMapping("modifierMainMapping");
        JPQLQuery<String> modifierMainBlogFidSub = getMainBlogFidSubquery(modifierMainBlog, modifierMainMapping, modifier);

        List<Tuple> result = queryFactory
                .select(
                        post.postId,
                        post.postTitle,
                        post.postContent,
                        post.postPublic,
                        post.viewCount,
                        post.createdAt,
                        post.updatedAt,
                        writer.mbUuid,
                        writerMapping.mbNickname,
                        writerMainBlogFidSub,
                        modifier.mbUuid,
                        modifierMapping.mbNickname,
                        modifierMainBlogFidSub,
                        category.categoryName
                )
                .from(post)
                    .leftJoin(writer).on(writer.eq(post.writer))
                    .leftJoin(writerMapping).on(writerMapping.member.eq(writer).and(writerMapping.blog.eq(post.blog)))
                    .leftJoin(modifier).on(modifier.eq(post.modifier))
                    .leftJoin(modifierMapping).on(modifierMapping.member.eq(modifier).and(modifierMapping.blog.eq(post.blog)))
                    .leftJoin(pcm).on(pcm.post.eq(post))
                    .leftJoin(category).on(category.eq(pcm.category))
                .where(post.postId.eq(postId))
                .fetch();

        if (result.isEmpty()) return Optional.empty();

        Tuple first = result.get(0);

        PostDetailResponse response = new PostDetailResponse(
                first.get(post.postId),
                new TeamBlogMemberResponse(
                        first.get(writer.mbUuid),
                        first.get(writerMapping.mbNickname),
                        first.get(writerMainBlogFidSub)
                ),
                new TeamBlogMemberResponse(
                        first.get(modifier.mbUuid),
                        first.get(modifierMapping.mbNickname),
                        first.get(modifierMainBlogFidSub)
                ),
                first.get(post.postTitle),
                first.get(post.postContent),
                first.get(post.postPublic),
                first.get(post.viewCount),
                first.get(post.createdAt),
                first.get(post.updatedAt),
                result.stream()
                        .map(t -> t.get(category.categoryName))
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList()
        );

        return Optional.of(response);
    }

    /**
     * 특정 멤버의 메인 블로그 FID를 가져오는 서브쿼리를 생성합니다.
     * <p>BlogMemberMapping이 ROLE_OWNER이고 blogMain이 true인 경우만 해당됩니다.</p>
     *
     * @param blog 블로그 Q타입
     * @param blogMemberMapping 블로그-멤버 매핑 Q타입
     * @param member 멤버 Q타입 (서브쿼리 기준이 되는 멤버)
     * @return 해당 멤버의 메인 블로그 FID 서브쿼리
     */
    private JPQLQuery<String> getMainBlogFidSubquery(QBlog blog, QBlogMemberMapping blogMemberMapping, QMember member) {
        return JPAExpressions.select(blog.blogFid)
                        .from(blog)
                                .join(blogMemberMapping).on(blogMemberMapping.blog.eq(blog))
                        .where(blogMemberMapping.member.eq(member)
                                .and(blogMemberMapping.role.roleId.eq("ROLE_OWNER"))
                                .and(blog.blogMain.isTrue()))
                        .limit(1);
    }
}
