package com.modeai.core.service;

import com.modeai.api.dto.LoginRequest;
import com.modeai.api.dto.LoginResponse;
import com.modeai.api.dto.RegisterRequest;
import com.modeai.common.dto.Result;
import com.modeai.common.exception.BusinessException;
import com.modeai.core.domain.entity.Role;
import com.modeai.core.domain.entity.User;
import com.modeai.core.domain.repository.RoleRepository;
import com.modeai.core.domain.repository.UserRepository;
import com.modeai.infra.security.JwtUtils;
import com.modeai.infra.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public Result<LoginResponse> login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        if (!user.getEnabled()) {
            throw new BusinessException(403, "账号已被禁用");
        }

        if (user.getLocked()) {
            throw new BusinessException(403, "账号已被锁定");
        }

        List<String> roles = user.getRoles().stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());

        List<String> permissions = new ArrayList<>();
        user.getRoles().forEach(role -> {
            permissions.add("ROLE_" + role.getRoleCode());
        });

        String token = jwtUtils.generateToken(user.getUsername(), roles, permissions);
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .nickname(user.getNickname())
                .roles(roles)
                .build();

        log.info("User logged in: {}", user.getUsername());
        return Result.success(response);
    }

    @Override
    @Transactional
    public Result<LoginResponse> register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(400, "用户名已存在");
        }

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(400, "邮箱已被注册");
        }

        Role defaultRole = roleRepository.findByRoleCode("ROLE_USER")
                .orElseThrow(() -> new BusinessException(500, "默认角色未配置"));

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .email(request.getEmail())
                .enabled(true)
                .locked(false)
                .roles(new java.util.HashSet<>())
                .build();

        user.getRoles().add(defaultRole);
        userRepository.save(user);

        List<String> roles = List.of("ROLE_USER");
        String token = jwtUtils.generateToken(user.getUsername(), List.of("ROLE_USER"), roles);
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .nickname(user.getNickname())
                .roles(List.of("ROLE_USER"))
                .build();

        log.info("User registered: {}", user.getUsername());
        return Result.success(response);
    }

    @Override
    public Result<LoginResponse> refreshToken(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BusinessException(401, "Refresh Token无效或已过期");
        }

        String username = jwtUtils.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));

        List<String> roles = user.getRoles().stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());

        List<String> permissions = roles.stream()
                .map(r -> "ROLE_" + r)
                .collect(Collectors.toList());

        String newToken = jwtUtils.generateToken(username, roles, permissions);
        String newRefreshToken = jwtUtils.generateRefreshToken(username);

        LoginResponse response = LoginResponse.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .username(user.getUsername())
                .roles(roles)
                .build();

        return Result.success(response);
    }

    @Override
    public Result<Void> logout(String token) {
        try {
            long ttl = jwtUtils.getExpirationTime(token) - System.currentTimeMillis();
            if (ttl > 0) {
                tokenBlacklistService.addToBlacklist(token, ttl);
            }
            log.info("User logged out");
            return Result.success();
        } catch (Exception e) {
            log.warn("Logout failed: {}", e.getMessage());
            return Result.success();
        }
    }
}
