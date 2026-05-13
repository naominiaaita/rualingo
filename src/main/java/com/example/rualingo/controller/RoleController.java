package com.example.rualingo.controller;

import com.example.rualingo.DTO.RoleDTO;
import com.example.rualingo.DTO.UserDTO;
import com.example.rualingo.service.RoleService;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@RequestBody RoleDTO roleDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(roleDTO));
    }

    @GetMapping
    public List<RoleDTO> getAllRoles(@RequestParam(required = false) String name) {
        if (name != null) {
            Optional<RoleDTO> role = roleService.getRoleByName(name);
            return role.map(List::of).orElseGet(List::of);
        }
        return roleService.getAllRoles();
    }

    @GetMapping("/{roleId}")
    public RoleDTO getRoleById(@PathVariable Long roleId) {
        return roleService.getRoleById(roleId);
    }

    @PutMapping("/{roleId}")
    public RoleDTO updateRole(@PathVariable Long roleId, @RequestBody RoleDTO roleDTO) {
        return roleService.updateRole(roleId, roleDTO);
    }

    @DeleteMapping("/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
    }

    @GetMapping("/{roleId}/users")
    public List<UserDTO> getUsersForRole(@PathVariable Long roleId) {
        return roleService.getUsersForRole(roleId);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
