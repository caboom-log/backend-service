package site.caboomlog.backendservice.topic.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class RootTopicResponse {
    private Integer topicId;
    private String topicName;
    private List<SubTopicResponse> subTopics = new ArrayList<>();
}
