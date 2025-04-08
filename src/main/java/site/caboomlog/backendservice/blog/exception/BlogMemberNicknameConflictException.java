package site.caboomlog.backendservice.blog.exception;

public class BlogMemberNicknameConflictException extends RuntimeException {
    public BlogMemberNicknameConflictException(String message) {
        super(message);
    }

    public BlogMemberNicknameConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
