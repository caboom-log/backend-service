package site.caboomlog.backendservice.topic.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import site.caboomlog.backendservice.common.dto.CommonListResponse;
import site.caboomlog.backendservice.topic.dto.RootTopicResponse;
import site.caboomlog.backendservice.topic.service.TopicService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping("/api/topics")
    public ResponseEntity<CommonListResponse<RootTopicResponse>> getAllRootTopics() {
        List<RootTopicResponse> rootTopicList = topicService.getAllRootTopics();
        return ResponseEntity.ok(new CommonListResponse<>(rootTopicList));
    }
}
