package com.tritech.tricore.core.domain.primarykeys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Represents a composite key for the `GroupLevel` entity.
 * <p>
 * This class is used to uniquely identify a `GroupLevel` entity through a combination of
 * the following fields:
 * <ul>
 * <li>`groupName`: The name of the group associated with the level.</li>
 * <li>`levelName`: The name of the level within the group.</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupLevelId implements Serializable {

	/**
	 * The name of the group.
	 */
	private String groupName;

	/**
	 * The name of the level within the group.
	 */
	private String levelName;

}
