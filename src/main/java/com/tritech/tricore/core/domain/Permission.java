package com.tritech.tricore.core.domain;

import com.tritech.tricore.core.domain.primarykeys.PermissionId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Represents a permission entity within the system. This class is mapped to the
 * "permissions" table in the database.
 * <p>
 * Each permission is uniquely identified by a combination of the following
 * fields:
 * <ul>
 *     <li>subject: The ID of the user associated with the permission.</li>
 *     <li>groupName: The name of the group associated with the permission.</li>
 *     <li>levelName: The name of the level associated with the permission.</li>
 * </ul>
 * <p>
 * This class includes the following:
 * <ul>
 *     <li>Fields for technical metadata, such as versioning and audit
 *     timestamps
 *         (e.g., createdAt, updatedAt).</li>
 *     <li>Relationships to the `User` and `GroupLevel` entities. These
 *     relationships
 *         define associations with a user and a specific group-level
 *         combination.</li>
 * </ul>
 * <p>
 * The `Permission` class utilizes composite keys defined by the
 * `PermissionId` class.
 */
@Entity
@Table(name = "permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(PermissionId.class)
@EntityListeners(AuditingEntityListener.class)
public class Permission {

    /**
     * Represents the unique identifier of a user associated with a permission.
     * This field is a part of the composite primary key for the `Permission`
     * entity and is used to establish a relationship with the `User` entity.
     * <p>
     * This field is mapped to the "subject" column in the "permissions" table
     * and must not be null.
     */
    @Id
    @Column(name = "subject", nullable = false)
    private Long subject;

    /**
     * Represents the name of the group associated with a permission. This field
     * acts as a part of the composite primary key for the `Permission` entity
     * and is mapped to the "group_name" column in the database table
     * corresponding to the `Permission` entity.
     * <p>
     * This field must be non-null and is associated with the group details
     * within the system.
     */
    @Id
    @Column(name = "group_name", nullable = false)
    private String groupName;

    /**
     * Represents the name of the level associated with a permission. This field
     * is a composite key component used to uniquely identify a `Permission`
     * entity in combination with `subject` and `groupName`.
     * <p>
     * It is mapped to the "level_name" column in the database and must not be
     * null.
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
     * Represents the association between a permission and a user entity in the
     * system. This field establishes a many-to-one relationship where multiple
     * permissions are associated with a single user.
     * <p>
     * The association is configured to be lazy-loaded, meaning that the user
     * entity is fetched from the database only when accessed. This helps
     * optimize performance by deferring data retrieval until it is explicitly
     * needed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject", referencedColumnName = "subject",
            foreignKey = @ForeignKey(name = "fk_permission_user"),
            insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    /**
     * Represents the relationship between the `Permission` entity and the
     * `GroupLevel` entity.
     * <p>
     * This field defines a many-to-one association with the `GroupLevel`
     * entity, where multiple `Permission` records can be associated with a
     * single `GroupLevel`. The association is established through the composite
     * key of `GroupLevel`, comprising `group_name` and `level_name`.
     * <p>
     * The database columns `group_name` and `level_name` in the `Permission`
     * table are used to reference the corresponding `group_name` and
     * `level_name` columns in the `GroupLevel` table.
     * <p>
     * The association is lazy-loaded, meaning the `GroupLevel` entity data is
     * fetched from the database only when it is explicitly accessed. This
     * improves performance by reducing unnecessary database operations.
     * <p>
     * If a GroupLevel in the `GroupLevel` entity is deleted, all associated
     * Permission will be automatically deleted in cascade.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = {
            @JoinColumn(name = "group_name",
                    referencedColumnName = "group_name",
                    insertable = false, updatable = false),
            @JoinColumn(name = "level_name",
                    referencedColumnName = "level_name",
                    insertable = false, updatable = false)
    }, foreignKey = @ForeignKey(name = "fk_permission_group_level"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GroupLevel groupLevel;

}
