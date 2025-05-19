package com.tritech.tricore.core.domain;

import com.tritech.tricore.core.domain.primarykeys.GroupLevelId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Represents a {@code GroupLevel} entity within the system.
 * <p>
 * This class is mapped to the {@code group_levels} table in the database.
 * </p>
 * <p>
 * A GroupLevel is uniquely identified by a combination of the following fields:
 * </p>
 * <ul>
 * <li>{@code groupName}: The name of the group associated with this level.</li>
 * <li>{@code levelName}: The name of the level within the group.</li>
 * </ul>
 * <p>
 * This class includes the following:
 * </p>
 * <ul>
 * <li>Technical fields for database versioning and audit tracking, such as
 * {@code createdAt} and {@code updatedAt} timestamps, and a {@code version} field for
 * optimistic locking.</li>
 * <li>A relationship to the {@link Group} entity, which establishes an association with
 * the group that this level belongs to.</li>
 * </ul>
 * <p>
 * This entity uses a composite key defined by the {@code GroupLevelId} class for its
 * unique identification.
 * </p>
 */
@Entity
@Table(name = "group_levels")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(GroupLevelId.class)
@EntityListeners(AuditingEntityListener.class)
public class GroupLevel {

	/**
	 * Represents the name of the group associated with the group level.
	 * <p>
	 * This field is a part of the composite primary key for the {@code GroupLevel} entity
	 * and establishes a relationship with the {@link Group} entity.
	 * </p>
	 *
	 * <p>
	 * It is mapped to the {@code group_name} column in the {@code group_levels} table and
	 * must not be {@code null}.
	 * </p>
	 */
	@Id
	@Column(name = "group_name", nullable = false)
	private String groupName;

	/**
	 * Represents the name of the level within a group.
	 * <p>
	 * This field is a part of the composite primary key for the {@code GroupLevel} entity
	 * and is used in combination with the {@code groupName} field to uniquely identify a
	 * group level.
	 * </p>
	 *
	 * <p>
	 * It is mapped to the {@code level_name} column in the {@code group_levels} table and
	 * must not be {@code null}.
	 * </p>
	 */
	@Id
	@Column(name = "level_name", nullable = false)
	private String levelName;

	// Technical Fields

	/**
	 * Represents the version of the entity used for optimistic locking purposes.
	 * <p>
	 * This field is managed by the persistence framework to ensure data consistency and
	 * prevent concurrent update conflicts during database transactions.
	 * </p>
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
	 * This field defines a many-to-one association between {@code GroupLevel} and
	 * {@code Group}, where multiple {@code GroupLevel} entities can be associated with a
	 * single {@code Group}.
	 * <p>
	 * The association is lazy-loaded, meaning the {@code Group} entity data is fetched
	 * from the database only when it is explicitly accessed.
	 * </p>
	 * <p>
	 * If a {@code Group} in the {@code Group} entity is deleted, all associated
	 * {@code GroupLevel} entities will be automatically deleted in cascade.
	 * </p>
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_name", referencedColumnName = "group_name",
			foreignKey = @ForeignKey(name = "fk_group_level_group"), insertable = false, updatable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Group group;

}
