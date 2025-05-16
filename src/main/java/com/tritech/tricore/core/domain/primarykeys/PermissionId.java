package com.tritech.tricore.core.domain.primarykeys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionId implements Serializable {
    private Integer subject;
    private String groupName;
    private String levelName;
}

