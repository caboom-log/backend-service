package site.caboomlog.backendservice.topic.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SubTopicResponse {
    private Integer topicId;
    private String topicName;
}
