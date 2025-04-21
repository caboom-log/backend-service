package site.caboomlog.backendservice.common.image.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ImageDto {
    @NotNull
    private String filename;
    @NotNull
    private Long size;
    private Integer width;
    private Integer height;
    @NotNull
    private String url;
    @NotNull
    private Integer imageOrder;
}
