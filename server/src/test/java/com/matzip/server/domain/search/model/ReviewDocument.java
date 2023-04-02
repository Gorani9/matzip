package com.matzip.server.domain.search.model;

import com.matzip.server.domain.review.model.Review;
import org.springframework.data.elasticsearch.annotations.*;

import javax.persistence.Id;
import java.time.LocalDateTime;

import static org.springframework.data.elasticsearch.annotations.DateFormat.date_hour_minute_second_millis;
import static org.springframework.data.elasticsearch.annotations.DateFormat.epoch_millis;

@Document(indexName = "review")
@Mapping(mappingPath = "elastic/review-mapping.json")
@Setting(settingPath = "elastic/review-setting.json")
public class ReviewDocument {
    @Id
    private Long id;
    private String content;
    private Integer rating;
    private String restaurant;
    private Long views;
    @Field(type = FieldType.Date, format = {date_hour_minute_second_millis, epoch_millis})
    private LocalDateTime createdAt;
    @Field(type = FieldType.Date, format = {date_hour_minute_second_millis, epoch_millis})
    private LocalDateTime modifiedAt;

    public ReviewDocument() {
    }

    public ReviewDocument(Review review) {
        this.id = review.getId();
        this.content = review.getContent();
        this.rating = review.getRating();
        this.restaurant = review.getRestaurant();
        this.views = review.getViews();
        this.createdAt = review.getCreatedAt();
        this.modifiedAt = review.getModifiedAt();
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Integer getRating() {
        return rating;
    }

    public String getRestaurant() {
        return restaurant;
    }

    public Long getViews() {
        return views;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }
}
