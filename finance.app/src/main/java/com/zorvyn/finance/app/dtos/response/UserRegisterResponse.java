package com.zorvyn.finance.app.dtos.response;

import com.zorvyn.finance.app.entity.enums.Role;
import com.zorvyn.finance.app.entity.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserRegisterResponse {
    private UUID id;
    private String userName;
    private String email;
    private Role role;
    private Status status;
}
