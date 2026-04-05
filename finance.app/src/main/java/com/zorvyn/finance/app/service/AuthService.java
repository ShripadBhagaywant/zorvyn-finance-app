package com.zorvyn.finance.app.service;

import com.zorvyn.finance.app.dtos.request.UserLoginRequest;
import com.zorvyn.finance.app.dtos.response.UserLoginResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    UserLoginResponse login(UserLoginRequest request, HttpServletResponse response);
    void logout(String token, HttpServletResponse response);
}
