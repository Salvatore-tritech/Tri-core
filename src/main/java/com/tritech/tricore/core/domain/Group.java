package com.tritech.tricore.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

/**
 * Represents a group entity within the system. This class is mapped to the
 * "groups" table in the database.
 * <p>
 * Each Group is uniquely identified by its name (groupName) which must be both
 * unique and non-null.
 * <p>
 * This class includes fields for:
 * <ul>
 *     <li>Database versioning using an optimistic locking
 *     strategy through the version field.</li>
 *     <li>Audit tracking, specifically the `createdAt` and `updatedAt`
 *     timestamps which store
 *     the record creation and last update times respectively.</li>
 * </ul>
 * <p>
 * The `Group` entity is referenced by other entities,
 * such as `GroupLevel`, to establish
 * associations within the domain model.
 */
@Entity
@Table(name = "groups")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group {

    /**
     * Represents the unique name of a group within the system. This field is
     * mapped to the "group_name" column in the "groups" table.
     */
    @Id
    @Column(name = "group_name", nullable = false, unique = true)
    private String groupName;

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

}
