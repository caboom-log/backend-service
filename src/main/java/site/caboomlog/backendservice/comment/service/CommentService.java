package site.caboomlog.backendservice.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import site.caboomlog.backendservice.blogmember.entity.BlogMemberMapping;
import site.caboomlog.backendservice.blogmember.repository.BlogMemberMappingRepository;
import site.caboomlog.backendservice.comment.dto.CommentResponse;
import site.caboomlog.backendservice.comment.entity.Comment;
import site.caboomlog.backendservice.comment.repository.CommentRepository;
import site.caboomlog.backendservice.role.entity.Role;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BlogMemberMappingRepository blogMemberMappingRepository;

    public Page<CommentResponse> getAllComments(Long mbNo, Long postId, String blogFid, Pageable pageable) {

        // 일단 pageable 에 따라서 모든 댓글 조회하기
        Page<Comment> comments = commentRepository.findAllByPost_PostId(postId, pageable);

        // 만약 mbNo가 null이 아니고 해당 블로그에 대한 권한이 있으면, 비공개 댓글까지 모두 조회
        if (hasRole(mbNo, blogFid)) {
            return comments.map(c -> {
                CommentResponse commentResponse = CommentResponse.fromEntity(c);
                BlogMemberMapping blogMemberMapping = blogMemberMappingRepository
                        .findByMember_MbNoAndBlog_BlogMain(c.getBlogMemberMapping().getMember().getMbNo(), true);
                commentResponse.setMbProfile(blogMemberMapping.getBlog().getBlogMainImg());
                commentResponse.setMbNickname(blogMemberMapping.getMbNickname());
                commentResponse.setMbMainBlogFid(blogMemberMapping.getBlog().getBlogFid());
                return commentResponse;
            });
        } else {
            // 그렇지 않으면, 비공개 댓글은 -비공개 댓글입니다.- 로 조회되도록 하기
            return comments.map(c -> {
                CommentResponse commentResponse = CommentResponse.fromEntityWithMasking(c);
                BlogMemberMapping blogMemberMapping = blogMemberMappingRepository
                        .findByMember_MbNoAndBlog_BlogMain(c.getBlogMemberMapping().getMember().getMbNo(), true);
                commentResponse.setMbProfile(blogMemberMapping.getBlog().getBlogMainImg());
                commentResponse.setMbNickname(blogMemberMapping.getMbNickname());
                commentResponse.setMbMainBlogFid(blogMemberMapping.getBlog().getBlogFid());
                return commentResponse;
            });
        }
    }

    private boolean hasRole(Long mbNo, String blogFid) {
        if (mbNo == null) {
            return false;
        }
        BlogMemberMapping mapping = blogMemberMappingRepository.findByMember_MbNoAndBlog_BlogFid(mbNo, blogFid);
        Role role = mapping.getRole();
        return "ROLE_MEMBER".equals(role.getRoleId()) || "ROLE_ADMIN".equals(role.getRoleId());
    }
}
