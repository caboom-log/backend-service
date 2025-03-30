package site.caboomlog.backendservice.topic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.caboomlog.backendservice.topic.entity.Topic;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Integer> {
    List<Topic> findByParentTopicIsNullOrderByTopicSeqAsc();
}
