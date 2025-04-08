package site.caboomlog.backendservice.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BlogInfoResponse {
    private String blogName;
    private String blogDesc;
    private String blogMainImg;
}
