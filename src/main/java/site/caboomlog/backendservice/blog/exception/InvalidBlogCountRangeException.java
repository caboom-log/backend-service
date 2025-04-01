package site.caboomlog.backendservice.blog.exception;

public class InvalidBlogCountRangeException extends RuntimeException {
    public InvalidBlogCountRangeException(String message) {
        super(message);
    }

    public InvalidBlogCountRangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
