package com.tritech.tricore.core.port.output;

import com.tritech.tricore.core.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepositoryPort extends JpaRepository<User, Long> {

}
