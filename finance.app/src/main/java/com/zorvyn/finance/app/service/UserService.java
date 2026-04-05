package com.zorvyn.finance.app.service;

import com.zorvyn.finance.app.dtos.request.UserRegisterRequestDto;
import com.zorvyn.finance.app.dtos.response.PageResponse;
import com.zorvyn.finance.app.dtos.response.UserRegisterResponse;
import com.zorvyn.finance.app.entity.enums.Role;
import com.zorvyn.finance.app.entity.enums.Status;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserRegisterResponse registerUser(UserRegisterRequestDto request);

    PageResponse<UserRegisterResponse> getAllUsers(String email, Role role, Status status, Pageable pageable);

    UserRegisterResponse getUserById(String id);

    UserRegisterResponse updateUserRole(String id, Role newRole);

    UserRegisterResponse updateUserStatus(String id, Status newStatus);

    void deleteUser(String id);

}
