package com.modeai.controller.admin;

import com.modeai.common.dto.Result;
import com.modeai.core.domain.entity.Role;
import com.modeai.core.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final RoleRepository roleRepository;

    @GetMapping
    public Result<List<Role>> listRoles() {
        return Result.success(roleRepository.findAll());
    }

    @GetMapping("/{id}")
    public Result<Role> getRole(@PathVariable Long id) {
        return Result.success(roleRepository.findById(id).orElse(null));
    }

    @PostMapping
    public Result<Role> createRole(@RequestBody Role role) {
        return Result.success(roleRepository.save(role));
    }

    @PutMapping("/{id}")
    public Result<Role> updateRole(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        return Result.success(roleRepository.save(role));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleRepository.deleteById(id);
        return Result.success();
    }
}
