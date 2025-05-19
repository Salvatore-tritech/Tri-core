package com.tritech.tricore.core.application.mapper;

import com.tritech.tricore.shared.dto.UserInfoDTO;
import org.apache.ibatis.annotations.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface UserMapper {

	UserMapper INSTANCE = new UserMapperImpl();

	default UserInfoDTO mapToUserInfoDTO(OidcUser oidcUser){
		return new UserInfoDTO(
				oidcUser.getFullName(),
				oidcUser.getEmail(),
				oidcUser.getPicture()
		);
	}
}
