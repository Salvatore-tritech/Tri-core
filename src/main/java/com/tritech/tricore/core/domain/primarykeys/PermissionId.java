package com.tritech.tricore.core.domain.primarykeys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Represents a composite key for the {@link com.tritech.tricore.core.domain.Permission
 * Permission} entity.
 * <p>
 * This class is used to uniquely identify a
 * {@link com.tritech.tricore.core.domain.Permission Permission} through a combination of
 * the following fields:
 * </p>
 * <ul>
 * <li>{@code subject}: The unique identifier of the user associated with the
 * permission.</li>
 * <li>{@code groupName}: The name of the group associated with the permission.</li>
 * <li>{@code levelName}: The name of the level associated with the permission.</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionId implements Serializable {

	/**
	 * The unique identifier of the user associated with the permission.
	 */
	private Long subject;

	/**
	 * The name of the group associated with the permission.
	 */
	private String groupName;

	/**
	 * The name of the level associated with the permission.
	 */
	private String levelName;

}
