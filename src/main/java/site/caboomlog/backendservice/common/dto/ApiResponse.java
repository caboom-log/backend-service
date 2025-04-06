package site.caboomlog.backendservice.common.dto;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private String status;
    private int code;
    private String message;
    private T content;

    protected ApiResponse(){}

    protected ApiResponse(String status, int code, String message, T content) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.content = content;
    }

    public static <T> ApiResponse<T> ok(T content, String message) {
        return new ApiResponse<>("SUCCESS", 200, message, content);
    }

    public static <T> ApiResponse<T> ok(T content) {
        return new ApiResponse<>("SUCCESS", 200, null, content);
    }

    public static <T> ApiResponse<T> created(T content, String message) {
        return new ApiResponse<>("SUCCESS", 201, message, content);
    }

    public static <T> ApiResponse<T> created() {
        return new ApiResponse<>("SUCCESS", 201, null, null);
    }

    public static <T> ApiResponse<T> success(int code, T content, String message) {
        return new ApiResponse<>("SUCCESS", code, message, content);
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>("ERROR", 404, message, null);
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>("ERROR", 400, message, null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>("ERROR", code, message, null);
    }
}
