package site.caboomlog.backendservice.post.entity;

import jakarta.persistence.*;
import site.caboomlog.backendservice.category.entity.Category;

@Entity
@Table(name = "post_category_mappings")
public class PostCategoryMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_category_mapping_id")
    private Long postCategoryMappingId;

    @JoinColumn(name = "category_id")
    @ManyToOne
    private Category category;

    @JoinColumn(name = "post_id")
    @ManyToOne
    private Post post;

    protected PostCategoryMapping(){}

    private PostCategoryMapping(Long postCategoryMappingId, Category category, Post post) {
        this.postCategoryMappingId = postCategoryMappingId;
        this.category = category;
        this.post = post;
    }

    public static PostCategoryMapping ofNewPostCategoryMapping(Category category, Post post) {
        return new PostCategoryMapping(null, category, post);
    }
}
