package site.caboomlog.backendservice.topic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.caboomlog.backendservice.topic.dto.RootTopicResponse;
import site.caboomlog.backendservice.topic.entity.Topic;
import site.caboomlog.backendservice.topic.repository.TopicRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;

    public List<RootTopicResponse> getAllRootTopics() {
        List<Topic> topicList = topicRepository.findByParentTopicIsNullOrderByTopicSeqAsc();
        return topicList.stream()
                .map(Topic::toRootTopicResponse)
                .toList();
    }
}
