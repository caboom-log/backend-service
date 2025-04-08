package site.caboomlog.backendservice.topic.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import site.caboomlog.backendservice.common.dto.ApiResponse;
import site.caboomlog.backendservice.topic.dto.RootTopicResponse;
import site.caboomlog.backendservice.topic.service.TopicService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping("/api/topics")
    public ResponseEntity<ApiResponse<List<RootTopicResponse>>> getAllRootTopics() {
        List<RootTopicResponse> topics = topicService.getAllRootTopics();
        return ResponseEntity.ok()
                .body(ApiResponse.ok(topics));
    }
}
