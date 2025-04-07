package site.caboomlog.backendservice.blog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "blogs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blog_id")
    private Long blogId;

    @Column(name = "blog_fid")
    private String blogFid;

    @Column(name = "blog_main", columnDefinition = "tinyint")
    private Boolean blogMain;

    @Column(name = "blog_name")
    private String blogName;

    @Column(name = "blog_description")
    private String blogDescription;

    @Column(name = "blog_public", columnDefinition = "tinyint")
    private Boolean blogPublic = true;

    @Column(name = "blog_main_img")
    private String blogMainImg;

    @Column(name = "blog_type")
    @Convert(converter = BlogTypeConverter.class)
    private BlogType blogType;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private  LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Blog(Long blogId, String blogFid, Boolean blogMain, String blogName,
                 String blogDescription, Boolean blogPublic, String blogMainImg,
                 BlogType blogType, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.blogId = blogId;
        this.blogFid = blogFid;
        this.blogMain = blogMain;
        this.blogName = blogName;
        this.blogDescription = blogDescription;
        this.blogPublic = blogPublic;
        this.blogMainImg = blogMainImg;
        this.blogType = blogType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Blog ofNewBlog(String blogFid, Boolean blogMain, String blogName, String blogDescription,
                                 boolean blogPublic, BlogType blogType) {
        return new Blog(null, blogFid, blogMain, blogName,
                blogDescription, blogPublic, null, blogType, null, null);
    }

    public static Blog ofExistingBlog(Long blogId, String blogFid, Boolean blogMain, String blogName,
                                      String blogDescription, Boolean blogPublic, String blogMainImg,
                                      BlogType blogType) {
        return new Blog(blogId, blogFid, blogMain, blogName,
                blogDescription, blogPublic, blogMainImg, blogType, null, null);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void setBlogMain() {
        this.blogMain = true;
    }

    private void setBlogNotMain() {
        this.blogMain = false;
    }

    public static void changeMainBlog(Blog oldMain, Blog newMain) {
        oldMain.setBlogNotMain();
        newMain.setBlogMain();
    }

    public void modifyBlogInfo(String blogName, String blogDescription, boolean blogPublic) {
        if (blogName != null) {
            this.blogName = blogName;
        }
        if (blogDescription != null) {
            this.blogDescription = blogDescription;
        }
        if (blogPublic != this.blogPublic) {
            this.blogPublic = blogPublic;
        }
    }
}
