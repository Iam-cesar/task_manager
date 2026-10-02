package com.example.task_manager.entities;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Getter
@MappedSuperclass
public abstract class BaseEntity {
    @Setter
    @Version
    private Integer version;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant created_at;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updated_at;

}
