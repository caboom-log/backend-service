package site.caboomlog.backendservice.common.exception;

public class UnAuthenticatedException extends RuntimeException {
    public UnAuthenticatedException(String message) {
        super(message);
    }

    public UnAuthenticatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
