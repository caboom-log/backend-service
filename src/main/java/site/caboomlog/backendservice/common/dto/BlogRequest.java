package site.caboomlog.backendservice.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BlogRequest {
    private String blogFid;

    private String blogName;

    private String blogMainImg;

    private String blogDescription;

    private String blogType;
}

