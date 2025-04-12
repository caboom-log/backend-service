package site.caboomlog.backendservice.category.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import site.caboomlog.backendservice.blog.entity.Blog;
import site.caboomlog.backendservice.topic.entity.Topic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @JoinColumn(name = "blog_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Blog blog;

    @JoinColumn(name = "category_pid")
    @ManyToOne(fetch = FetchType.LAZY)
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> childCategories = new ArrayList<>();

    @JoinColumn(name = "topic_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Topic topic;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "category_public", columnDefinition = "tinyint")
    private Boolean categoryPublic;

    @Column(name = "category_order")
    private Long categoryOrder;

    @Column(name = "depth")
    private Long depth;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Category(){}

    private Category(Long categoryId, Blog blog, Category parentCategory, Topic topic,
                     String categoryName, Boolean categoryPublic, Long categoryOrder,
                     Long depth, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.categoryId = categoryId;
        this.blog = blog;
        this.parentCategory = parentCategory;
        this.topic = topic;
        this.categoryName = categoryName;
        this.categoryPublic = categoryPublic;
        this.categoryOrder = categoryOrder;
        this.depth = depth;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Category ofNewCategory(Blog blog, Category parentCategory, Topic topic,
                                         String categoryName, Boolean categoryPublic, Long categoryOrder,
                                         Long depth) {
        return new Category(null, blog, parentCategory, topic,
                categoryName, categoryPublic, categoryOrder, depth, null, null);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void changeVisibility(boolean categoryPublic) {
        this.categoryPublic = categoryPublic;
    }

    public static void switchOrder(Category category1, Category category2) {
        long order = category1.getCategoryOrder();
        category1.categoryOrder = category2.getCategoryOrder();
        category2.categoryOrder = order;
    }
}
