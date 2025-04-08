package site.caboomlog.backendservice.member.exception;

public class MemberWithdrawException extends RuntimeException {
    public MemberWithdrawException(String message) {
        super(message);
    }

    public MemberWithdrawException(String message, Throwable cause) {
        super(message, cause);
    }
}
