package com.zorvyn.finance.app.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginResponse{

        private String accessToken;

        @Builder.Default
        private String tokenType = "Bearer";

        private UserRegisterResponse user;

}
