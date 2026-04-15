package com.modeai.controller.admin;

import com.modeai.common.dto.Result;
import com.modeai.core.domain.entity.Department;
import com.modeai.core.domain.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/departments")
@RequiredArgsConstructor
public class AdminDepartmentController {

    private final DepartmentRepository departmentRepository;

    @GetMapping
    public Result<List<Department>> listDepartments() {
        return Result.success(departmentRepository.findAll());
    }

    @GetMapping("/{id}")
    public Result<Department> getDepartment(@PathVariable Long id) {
        return Result.success(departmentRepository.findById(id).orElse(null));
    }

    @PostMapping
    public Result<Department> createDepartment(@RequestBody Department department) {
        return Result.success(departmentRepository.save(department));
    }

    @PutMapping("/{id}")
    public Result<Department> updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        department.setId(id);
        return Result.success(departmentRepository.save(department));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteDepartment(@PathVariable Long id) {
        departmentRepository.deleteById(id);
        return Result.success();
    }
}
