package com.tritech.tricore.core.domain.primarykeys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Represents a composite key for the
 * {@link com.tritech.tricore.core.domain.GroupLevel GroupLevel} entity.
 * <p>
 * This class is used to uniquely identify a {@link com.tritech.tricore.core.domain.GroupLevel GroupLevel} entity
 * through a
 * combination of the following fields:
 * </p>
 * <ul>
 *     <li>{@code groupName}: The name of the group associated with the level.</li>
 *     <li>{@code levelName}: The name of the level within the group.</li>
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
