package com.modeai.core.service;

import com.modeai.api.dto.LoginRequest;
import com.modeai.api.dto.LoginResponse;
import com.modeai.api.dto.RegisterRequest;
import com.modeai.common.dto.Result;

public interface AuthService {
    Result<LoginResponse> login(LoginRequest request);
    Result<LoginResponse> register(RegisterRequest request);
    Result<LoginResponse> refreshToken(String refreshToken);
    Result<Void> logout(String token);
}
