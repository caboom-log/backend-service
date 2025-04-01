package site.caboomlog.backendservice.member.exception;

public class MainBlogNotFoundException extends RuntimeException {
    public MainBlogNotFoundException(String message) {
        super(message);
    }
}
