package site.caboomlog.backendservice.common.adaptor;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import site.caboomlog.backendservice.common.dto.BlogRequest;
import site.caboomlog.backendservice.common.dto.PostRequest;

@FeignClient(name = "search-service")
public interface SearchServiceAdaptor {

    @PostMapping("/api/blogs")
    ResponseEntity<String> createBlog(@RequestBody BlogRequest blogRequest);

    @DeleteMapping("/blogs/{blogFid}")
    ResponseEntity<String> deleteBlog(@PathVariable("blogFid") String blogFid);

    @PostMapping("/api/posts")
    ResponseEntity<String> createPost(@RequestBody PostRequest postRequest);

    @DeleteMapping("/api/posts/{postId}")
    ResponseEntity<String> deletePost(@PathVariable("postId") Long postId);
}
