package com.matzip.server.domain.image.model;

import com.matzip.server.global.common.model.BaseLazyDeletedTimeEntity;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;

@MappedSuperclass
@NoArgsConstructor
public class BaseImageEntity extends BaseLazyDeletedTimeEntity {
    @NotBlank
    @URL
    private String imageUrl;

    public BaseImageEntity(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        if (this.isBlocked()) return null;
        else return imageUrl;
    }
}
