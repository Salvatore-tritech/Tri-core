package com.tritech.tricore.core.port.output;

import com.tritech.tricore.core.domain.GroupLevel;
import com.tritech.tricore.core.domain.primarykeys.GroupLevelId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupLevelRepositoryPort extends JpaRepository<GroupLevel, GroupLevelId> {

	/**
	 * Retrieves a list of {@link GroupLevel} entities associated with the specified group
	 * name. This method queries the database for all group levels that share the given
	 * group name value.
	 * @param groupName the name of the group whose associated levels are to be retrieved;
	 * must not be null.
	 * @return a list of {@link GroupLevel} entities matching the provided group name; if
	 * no matches are found, an empty list is returned.
	 */
	List<GroupLevel> findByGroupName(String groupName);

}
