package site.caboomlog.backendservice.common.image.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import site.caboomlog.backendservice.post.entity.Post;

@Entity
@Table(name = "post_image_mappings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImageMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_image_mapping_id")
    private Long postImageMappingId;

    @JoinColumn(name = "post_id")
    @ManyToOne
    private Post post;

    @JoinColumn(name = "image_id")
    @ManyToOne
    private Image image;

    @Column
    private Integer width;

    @Column
    private Integer height;

    @Column(name = "image_order")
    private Integer imageOrder;

    private PostImageMapping(Long postImageMappingId, Post post, Image image,
                             Integer width, Integer height, Integer imageOrder) {
        this.postImageMappingId = postImageMappingId;
        this.post = post;
        this.image = image;
        this.width = width;
        this.height = height;
        this.imageOrder = imageOrder;
    }

    public static PostImageMapping ofNewPostImageMapping(Post post, Image image,
                                                         Integer width, Integer height, Integer imageOrder) {
        return new PostImageMapping(null, post, image, width, height, imageOrder);
    }
}
