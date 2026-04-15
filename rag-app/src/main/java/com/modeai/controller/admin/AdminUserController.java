package com.modeai.controller.admin;

import com.modeai.common.dto.PageResult;
import com.modeai.common.dto.Result;
import com.modeai.core.domain.entity.User;
import com.modeai.core.domain.repository.UserRepository;
import com.modeai.core.domain.repository.RoleRepository;
import com.modeai.core.domain.entity.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public Result<PageResult<User>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> result = userRepository.findAll(pageable);
        return Result.success(PageResult.of(result.getContent(), result.getTotalElements(), page, size));
    }

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        return Result.success(userRepository.findById(id).orElse(null));
    }

    @PostMapping
    public Result<User> createUser(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String email = (String) body.get("email");
        String nickname = (String) body.get("nickname");

        if (userRepository.existsByUsername(username)) {
            return Result.error(400, "用户名已存在");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .nickname(nickname)
                .enabled(true)
                .locked(false)
                .roles(new HashSet<>())
                .build();

        // Assign default role
        roleRepository.findByRoleCode("ROLE_USER").ifPresent(user.getRoles()::add);
        userRepository.save(user);

        return Result.success(user);
    }

    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return Result.error(404, "用户不存在");

        if (body.containsKey("nickname")) user.setNickname((String) body.get("nickname"));
        if (body.containsKey("email")) user.setEmail((String) body.get("email"));
        if (body.containsKey("enabled")) user.setEnabled((Boolean) body.get("enabled"));
        if (body.containsKey("locked")) user.setLocked((Boolean) body.get("locked"));

        userRepository.save(user);
        return Result.success(user);
    }

    @PutMapping("/{id}/roles")
    public Result<User> assignRoles(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return Result.error(404, "用户不存在");

        List<Long> roleIds = body.get("roleIds");
        if (roleIds != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
            user.setRoles(roles);
            userRepository.save(user);
        }
        return Result.success(user);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return Result.success();
    }
}
