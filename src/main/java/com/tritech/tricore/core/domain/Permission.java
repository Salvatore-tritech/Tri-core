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
 * {@code permissions} table in the database.
 * <p>
 * Each permission is uniquely identified by a combination of the following fields:
 * <ul>
 * <li>{@code subject}: The ID of the user associated with the permission .</li>
 * <li>{@code groupName}: The name of the group associated with the permission.</li>
 * <li>{@code levelName}: The name of the level associated with the permission.</li>
 * </ul>
 * </p>
 * <p>
 * This class includes the following:
 * <ul>
 * <li>Fields for technical metadata, such as versioning and audit timestamps (e.g.,
 * {@code createdAt}, {@code updatedAt}).</li>
 * <li>Relationships to the {@link User} and {@link GroupLevel} entities. These
 * relationships define associations with a user and a specific group-level
 * combination.</li>
 * </ul>
 * </p>
 * <p>
 * The {@code Permission} class utilizes composite keys defined by the
 * {@link PermissionId} class.
 * </p>
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
	 * Represents the unique identifier of a {@link User} associated with a permission.
	 * <p>
	 * This field is a part of the composite primary key for the {@code Permission} entity
	 * and is used to establish a relationship with the {@link User} entity.
	 * </p>
	 * <p>
	 * This field is mapped to the {@code subject} column in the {@code permissions} table
	 * and must not be null.
	 * </p>
	 */
	@Id
	@Column(name = "subject", nullable = false)
	private Long subject;

	/**
	 * Represents the name of the group associated with a permission.
	 * <p>
	 * This field acts as a part of the composite primary key for the {@code Permission}
	 * entity and is mapped to the {@code group_name} column in the database table
	 * corresponding to the {@code Permission} entity.
	 * </p>
	 * <p>
	 * This field must be non-null and is associated with the group details within the
	 * system.
	 * </p>
	 */
	@Id
	@Column(name = "group_name", nullable = false)
	private String groupName;

	/**
	 * Represents the name of the level associated with a permission.
	 * <p>
	 * This field is a composite key component used to uniquely identify a
	 * {@code Permission} entity in combination with {@code subject} and
	 * {@code groupName}.
	 * </p>
	 * <p>
	 * It is mapped to the {@code level_name} column in the database and must not be null.
	 * </p>
	 */
	@Id
	@Column(name = "level_name", nullable = false)
	private String levelName;

	// Technical Fields

	/**
	 * Represents the version of the entity for optimistic locking purposes. This field is
	 * used by the persistence framework to ensure consistency and prevent concurrent
	 * update conflicts during database transactions.
	 */
	@Version
	private Long version;

	/**
	 * Represents the creation timestamp of the entity. This field is automatically
	 * populated with the date and time when the entity is persisted for the first time.
	 */
	@Column(name = "created_at", nullable = false, updatable = false)
	@CreatedDate
	private java.time.LocalDateTime createdAt;

	/**
	 * Represents the timestamp of the last modification made to the entity. This field is
	 * automatically updated with the current date and time whenever the entity is updated
	 * in the database.
	 */
	@Column(name = "updated_at")
	@LastModifiedDate
	private java.time.LocalDateTime updatedAt;

	// Relations

	/**
	 * Represents the association between a {@code Permission} and a {@link User} entity
	 * in the system.
	 * <p>
	 * This field establishes a many-to-one relationship where multiple permissions are
	 * associated with a single user.
	 * </p>
	 * <p>
	 * The association is configured to be lazy-loaded, meaning that the user entity is
	 * fetched from the database only when accessed. This helps optimize performance by
	 * deferring data retrieval until it is explicitly needed.
	 * </p>
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subject", referencedColumnName = "subject",
			foreignKey = @ForeignKey(name = "fk_permission_user"), insertable = false, updatable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private User user;

	/**
	 * Represents the relationship between the {@code Permission} entity and the
	 * {@link GroupLevel} entity.
	 * <p>
	 * This field defines a many-to-one association with the {@link GroupLevel} entity,
	 * where multiple {@code Permission} records can be associated with a single
	 * {@link GroupLevel}. The association is established through the composite key of
	 * {@link GroupLevel}, comprising {@code group_name} and {@code level_name}.
	 * </p>
	 * <p>
	 * The database columns {@code group_name} and {@code level_name} in the
	 * {@code Permission} table are used to reference the corresponding {@code group_name}
	 * and {@code level_name} columns in the {@link GroupLevel} table.
	 * </p>
	 * <p>
	 * The association is lazy-loaded, meaning the {@link GroupLevel} entity data is
	 * fetched from the database only when it is explicitly accessed. This improves
	 * performance by reducing unnecessary database operations.
	 * </p>
	 * <p>
	 * If a {@link GroupLevel} in the {@link GroupLevel} entity is deleted, all associated
	 * {@code Permission} records will be automatically deleted in cascade.
	 * </p>
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns(value = {
			@JoinColumn(name = "group_name", referencedColumnName = "group_name", insertable = false,
					updatable = false),
			@JoinColumn(name = "level_name", referencedColumnName = "level_name", insertable = false,
					updatable = false) },
			foreignKey = @ForeignKey(name = "fk_permission_group_level"))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private GroupLevel groupLevel;

}
