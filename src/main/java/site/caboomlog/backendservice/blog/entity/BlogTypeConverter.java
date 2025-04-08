package site.caboomlog.backendservice.blog.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BlogTypeConverter implements AttributeConverter<BlogType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(BlogType attribute) {
        return attribute.getCode();
    }

    @Override
    public BlogType convertToEntityAttribute(Integer dbData) {
        return BlogType.fromCode(dbData);
    }
}
