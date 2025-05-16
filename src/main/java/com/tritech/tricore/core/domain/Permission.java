package com.tritech.tricore.core.domain;

import com.tritech.tricore.core.domain.primarykeys.PermissionId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Table(name = "permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(PermissionId.class)
public class Permission {

    @Id
    @Column(name = "subject", nullable = false)
    private Integer subject;

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
    @JoinColumn(name = "subject", referencedColumnName = "subject", foreignKey = @ForeignKey(name = "fk_permission_user"),
        insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = {
            @JoinColumn(name = "group_name", referencedColumnName = "group_name",
                    insertable = false, updatable = false),
            @JoinColumn(name = "level_name", referencedColumnName = "level_name",
                    insertable = false, updatable = false)
    }, foreignKey = @ForeignKey(name = "fk_permission_group_level"))
    private GroupLevel groupLevel;


}
