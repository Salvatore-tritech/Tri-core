package com.tritech.tricore.core.domain;

import com.tritech.tricore.core.domain.primarykeys.GroupLevelId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

/**
 * Represents a group level entity within the system. This class is mapped to
 * the "group_levels" table in the database.
 * <p>
 * A GroupLevel is uniquely identified by a combination of the following
 * fields:
 * <ul>
 *     <li>groupName: The name of the group associated with this level.</li>
 *     <li>levelName: The name of the level within the group.</li>
 * </ul>
 * <p>
 * This class includes the following:
 * <ul>
 *     <li>Technical fields for database versioning
 *     and audit tracking, such as createdAt
 *     and updatedAt timestamps, and a version
 *     field for optimistic locking.</li>
 *     <li>A relationship to the `Group` entity,
 *     which establishes an association with
 *     the group that this level belongs to.</li>
 * </ul>
 * <p>
 * This entity uses a composite key defined by
 * the `GroupLevelId` class for its unique
 * identification.
 */
@Entity
@Table(name = "group_levels")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(GroupLevelId.class)
public class GroupLevel {

    /**
     * Represents the name of the group associated with the group level. This
     * field is a part of the composite primary key for the `GroupLevel` entity
     * and establishes a relationship with the `Group` entity.
     * <p>
     * It is mapped to the "group_name" column in the "group_levels" table and
     * must not be null.
     */
    @Id
    @Column(name = "group_name", nullable = false)
    private String groupName;

    /**
     * Represents the name of the level within a group. This field is a part of
     * the composite primary key for the `GroupLevel` entity and is used in
     * combination with the `groupName` field to uniquely identify a group
     * level.
     * <p>
     * It is mapped to the "level_name" column in the "group_levels" table and
     * must not be null.
     */
    @Id
    @Column(name = "level_name", nullable = false)
    private String levelName;

    // Technical Fields

    /**
     * Represents the version of the entity for optimistic locking purposes.
     * This field is used by the persistence framework to ensure consistency and
     * prevent concurrent update conflicts during database transactions.
     */
    @Version
    private Long version;

    /**
     * Represents the creation timestamp of the entity. This field is
     * automatically populated with the date and time when the entity is
     * persisted for the first time.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private java.time.LocalDateTime createdAt;

    /**
     * Represents the timestamp of the last modification made to the entity.
     * This field is automatically updated with the current date and time
     * whenever the entity is updated in the database.
     */
    @Column(name = "updated_at")
    @LastModifiedDate
    private java.time.LocalDateTime updatedAt;

    // Relations

    /**
     * This field defines a many-to-one association between `GroupLevel` and
     * `Group`, where multiple `GroupLevel` entities can be associated with a
     * single `Group`.
     * <p>
     * The association is lazy-loaded, meaning the `Group` entity data is
     * fetched from the database only when it is explicitly accessed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_name", referencedColumnName = "group_name",
            foreignKey = @ForeignKey(name = "fk_group_level_group"),
            insertable = false, updatable = false)
    private Group group;

}
