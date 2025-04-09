package site.caboomlog.backendservice.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MyBlogInfoResponse {
    private String blogFid;
    private String blogName;
    private String blogType;
}
