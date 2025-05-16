package com.tritech.tricore.core.domain.primarykeys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Represents a composite key for the `Permission` entity.
 * <p>
 * This class is used to identify a `Permission` uniquely through a combination
 * of the following fields:
 * <ul>
 *     <li>`subject`: The unique identifier of the user associated with the
 *     permission.</li>
 *     <li>`groupName`: The name of the group associated with the permission
 *     .</li>
 *     <li>`levelName`: The name of the level associated with the permission
 *     .</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionId implements Serializable {
    /**
     * The unique identifier of the user associated with the permission.
     */
    private Integer subject;
    /**
     * The name of the group associated with the permission.
     */
    private String groupName;
    /**
     * The name of the level associated with the permission.
     */
    private String levelName;
}
