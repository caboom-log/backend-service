package site.caboomlog.backendservice.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.caboomlog.backendservice.blogmember.entity.BlogMemberMapping;
import site.caboomlog.backendservice.blogmember.repository.BlogMemberMappingRepository;
import site.caboomlog.backendservice.comment.dto.CommentResponse;
import site.caboomlog.backendservice.comment.dto.WriteCommentRequest;
import site.caboomlog.backendservice.comment.entity.Comment;
import site.caboomlog.backendservice.comment.repository.CommentRepository;
import site.caboomlog.backendservice.common.exception.UnauthenticatedException;
import site.caboomlog.backendservice.post.entity.Post;
import site.caboomlog.backendservice.post.exception.PostNotFoundException;
import site.caboomlog.backendservice.post.repository.PostRepository;
import site.caboomlog.backendservice.role.entity.Role;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BlogMemberMappingRepository blogMemberMappingRepository;
    private final PostRepository postRepository;

    /**
     * 특정 게시글의 모든 댓글을 조회합니다.
     * <p>블로그의 멤버일 경우 비공개 댓글도 모두 확인할 수 있으며, 그렇지 않은 경우 비공개 댓글은 마스킹됩니다.</p>
     *
     * @param mbNo 로그인한 사용자 번호 (비로그인 시 null)
     * @param postId 조회할 게시글 ID
     * @param blogFid 블로그 식별자
     * @param pageable 페이징 정보
     * @return 댓글 페이지 결과 (비공개 댓글은 조건에 따라 마스킹 처리)
     */
    @Transactional(readOnly = true)
    public Page<CommentResponse> getAllComments(Long mbNo, Long postId, String blogFid, Pageable pageable) {

        // 일단 pageable 에 따라서 모든 댓글 조회하기
        Page<Comment> comments = commentRepository.findAllByPost_PostId(postId, pageable);

        // 만약 mbNo가 null이 아니고 해당 블로그에 대한 권한이 있으면, 비공개 댓글까지 모두 조회
        if (hasRole(mbNo, blogFid)) {
            return comments.map(c -> {
                CommentResponse commentResponse = CommentResponse.fromEntity(c);
                BlogMemberMapping blogMemberMapping = blogMemberMappingRepository
                        .findByMember_MbNoAndBlog_BlogMain(c.getBlogMemberMapping().getMember().getMbNo(), true);
                commentResponse.setMbMainBlogFid(blogMemberMapping.getBlog().getBlogFid());
                return commentResponse;
            });
        } else {
            // 그렇지 않으면, 비공개 댓글은 -비공개 댓글입니다.- 로 조회되도록 하기
            return comments.map(c -> {
                CommentResponse commentResponse = CommentResponse.fromEntityWithMasking(c);
                BlogMemberMapping blogMemberMapping = blogMemberMappingRepository
                        .findByMember_MbNoAndBlog_BlogMain(c.getBlogMemberMapping().getMember().getMbNo(), true);
                commentResponse.setMbMainBlogFid(blogMemberMapping.getBlog().getBlogFid());
                return commentResponse;
            });
        }
    }

    /**
     * 댓글을 작성합니다.
     * <p>비공개 게시글에는 블로그 멤버만 댓글을 작성할 수 있으며, 공개 게시글에는 유효한 권한을 가진 계정만 댓글을 작성할 수 있습니다.</p>
     *
     * @param blogFid 블로그 식별자
     * @param postId 게시글 ID
     * @param mbNo 작성자 사용자 번호
     * @param request 댓글 작성 요청 DTO
     * @throws PostNotFoundException 게시글이 존재하지 않는 경우
     * @throws UnauthenticatedException 권한이 없는 사용자가 댓글을 작성하려는 경우
     */
    @Transactional
    public void writeComment(String blogFid, Long postId, Long mbNo, WriteCommentRequest request) {
        Post post = postRepository.findByPostId(postId)
                .orElseThrow(()->new PostNotFoundException("존재하지 않는 게시물입니다."));
        if (!post.isPostPublic()) {
            if (hasRole(mbNo, blogFid)) {
                BlogMemberMapping blogMemberMapping = blogMemberMappingRepository
                        .findByMember_MbNoAndBlog_BlogFid(mbNo, request.getMbBlogFid());
                Comment comment = Comment.ofNewComment(request.getParentCommentId(),
                        post, blogMemberMapping, request.getContent(), request.isCommentPublic());
                commentRepository.save(comment);
            } else {
                throw new UnauthenticatedException("댓글 작성 권한이 없습니다.");
            }
        } else {
            BlogMemberMapping blogMemberMapping = blogMemberMappingRepository
                    .findByMember_MbNoAndBlog_BlogFid(mbNo, request.getMbBlogFid());
            if (!Role.isValidRole(blogMemberMapping.getRole().getRoleId())) {
                throw new UnauthenticatedException("댓글을 작성할 수 없는 계정입니다.");
            }
            Comment comment = Comment.ofNewComment(request.getParentCommentId(),
                    post, blogMemberMapping, request.getContent(), request.isCommentPublic());
            commentRepository.save(comment);
        }

    }

    /**
     * 특정 블로그에 대해 사용자가 유효한 권한을 가지고 있는지 확인합니다.
     *
     * @param mbNo 사용자 번호 (nullable)
     * @param blogFid 블로그 식별자
     * @return 권한이 유효한 경우 true, 그렇지 않으면 false
     */
    private boolean hasRole(Long mbNo, String blogFid) {
        if (mbNo == null) {
            return false;
        }
        BlogMemberMapping mapping = blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(mbNo, blogFid);
        return mapping.getRole().isValidRole();
    }
}
