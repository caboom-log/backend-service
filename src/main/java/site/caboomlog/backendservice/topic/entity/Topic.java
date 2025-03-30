package site.caboomlog.backendservice.topic.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import site.caboomlog.backendservice.topic.dto.RootTopicResponse;
import site.caboomlog.backendservice.topic.dto.SubTopicResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "topics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_id")
    private Integer topicId;

    @JoinColumn(name = "topic_pid")
    @ManyToOne(fetch = FetchType.LAZY)
    private Topic parentTopic;

    @OneToMany(mappedBy = "parentTopic", fetch = FetchType.LAZY)
    private List<Topic> children = new ArrayList<>();


    @Column(name = "topic_name")
    private String topicName;

    @Column(name = "topic_seq")
    private Integer topicSeq;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Topic(Integer topicId, Topic parentTopic, String topicName, Integer topicSeq,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.topicId = topicId;
        this.parentTopic = parentTopic;
        this.topicName = topicName;
        this.topicSeq = topicSeq;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Topic ofExsitingTopic(Integer topicId, Topic parentTopic, String topicName, Integer topicSeq,
                                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Topic(topicId, parentTopic, topicName, topicSeq, createdAt, updatedAt);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public RootTopicResponse toRootTopicResponse() {
        List<SubTopicResponse> subTopics = this.getChildren().stream()
                .map(Topic::toSubTopicResponse)
                .toList();
        return new RootTopicResponse(this.getTopicId(), this.getTopicName(), subTopics);
    }

    public SubTopicResponse toSubTopicResponse() {
        return new SubTopicResponse(this.getTopicId(), this.getTopicName());
    }

}
