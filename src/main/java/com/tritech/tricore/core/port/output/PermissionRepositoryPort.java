package com.tritech.tricore.core.port.output;

import com.tritech.tricore.core.domain.Permission;
import com.tritech.tricore.core.domain.primarykeys.PermissionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepositoryPort extends JpaRepository<Permission, PermissionId> {

}
