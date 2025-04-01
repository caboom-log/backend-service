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

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private  LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "blog_main_img")
    private String blogMainImg;

    @Column(name = "blog_type")
    @Convert(converter = BlogTypeConverter.class)
    private BlogType blogType;

    private Blog(Long blogId, String blogFid, Boolean blogMain, String blogName,
                 String blogDescription, Boolean blogPublic, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.blogId = blogId;
        this.blogFid = blogFid;
        this.blogMain = blogMain;
        this.blogName = blogName;
        this.blogDescription = blogDescription;
        this.blogPublic = blogPublic;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Blog ofNewBlog(String blogFid, Boolean blogMain, String blogName, String blogDescription, boolean blogPublic) {
        return new Blog(null, blogFid, blogMain, blogName, blogDescription, blogPublic, null, null);
    }

    public static Blog ofExistingBlog(Long blogId, String blogFid, Boolean blogMain, String blogName,
                                      String blogDescription) {
        return new Blog(blogId, blogFid, blogMain, blogName, blogDescription,
                true, null, null);
    }

    public void setBlogPublic() {
        this.blogPublic = true;
    }

    public void setBlogPrivate() {
        this.blogPublic = false;
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
}
