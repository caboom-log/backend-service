package site.caboomlog.backendservice.blog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class BlogInfoResponse {
    private String blogName;
    private String blogDesc;
    private String blogMainImg;

    private boolean blogMain;
    private boolean blogPublic;
    private String blogType;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;
}
