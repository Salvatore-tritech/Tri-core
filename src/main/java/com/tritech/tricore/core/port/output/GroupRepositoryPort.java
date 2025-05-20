package com.tritech.tricore.core.port.output;

import com.tritech.tricore.core.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepositoryPort extends JpaRepository<Group, String> {

}
