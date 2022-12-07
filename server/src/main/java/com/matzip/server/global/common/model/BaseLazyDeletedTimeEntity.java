package com.matzip.server.global.common.model;

import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public class BaseLazyDeletedTimeEntity extends BaseTimeEntity {
    private boolean blocked = false;
    private boolean deleted = false;
    private String blockedReason = null;
    @DateTimeFormat
    private LocalDateTime deletedDate = null;

    public void block(String blockedReason) {
        this.blocked = true;
        this.blockedReason = blockedReason;
    }

    public void unblock() {
        this.blocked = false;
        this.blockedReason = null;
    }

    public void delete() {
        this.deleted = true;
        this.deletedDate = LocalDateTime.now();
    }

    public void restore() {
        this.deleted = false;
        this.deletedDate = null;
    }
}
