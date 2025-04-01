package site.caboomlog.backendservice.blog.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BlogType {
    PERSONAL(1),
    TEAM(2);

    private final int code;

    public static BlogType fromCode(int code) {
        for (BlogType type : BlogType.values()) {
            if (type.getCode() == code) return type;
        }
        throw new IllegalArgumentException("Invalid BlogType code: " + code);
    }
}
