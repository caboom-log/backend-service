package site.caboomlog.backendservice.blog.exception;

public class BlogFidDuplicatedException extends RuntimeException {
    public BlogFidDuplicatedException(String message) {
        super(message);
    }

    public BlogFidDuplicatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
