package com.huusang.demo.Mapper;

import com.huusang.demo.Dto.Request.UserCreationRequest;
import com.huusang.demo.Dto.Response.PermissionResponse;
import com.huusang.demo.Dto.Response.RoleResponse;
import com.huusang.demo.Dto.Response.UserResponse;
import com.huusang.demo.Entity.Permission;
import com.huusang.demo.Entity.Role;
import com.huusang.demo.Entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    RoleResponse toRoleResponse(Role role);
    PermissionResponse toPermissionResponse(Permission permission);

}
