package site.caboomlog.backendservice.blog.exception;

public class AlreadyInvitedException extends RuntimeException {
    public AlreadyInvitedException(String message) {
        super(message);
    }

    public AlreadyInvitedException(String message, Throwable cause) {
        super(message, cause);
    }
}
