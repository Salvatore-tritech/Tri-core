package com.tritech.tricore.core.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Table(name = "groups")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group {

    @Id
    @Column(name = "group_name", nullable = false, unique = true)
    private String groupName;

    // Technical Fields

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private java.time.LocalDateTime updatedAt;

}
