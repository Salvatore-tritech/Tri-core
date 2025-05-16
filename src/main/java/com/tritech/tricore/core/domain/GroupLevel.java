package com.tritech.tricore.core.domain;

import com.tritech.tricore.core.domain.primarykeys.GroupLevelId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Table(name = "group_levels")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(GroupLevelId.class)
public class GroupLevel {

    @Id
    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Id
    @Column(name = "level_name", nullable = false)
    private String levelName;

    // Technical Fields

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private java.time.LocalDateTime updatedAt;

    // Relations

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_name", referencedColumnName = "group_name", foreignKey = @ForeignKey(name = "fk_group_level_group"),
            insertable = false, updatable = false)
    private Group group;

}