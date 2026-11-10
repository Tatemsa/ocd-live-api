package com.ocdlive.ocd.user.controller;

import com.ocdlive.ocd.user.dto.UserResponse;
import com.ocdlive.ocd.user.mapper.UserMapper;
import com.ocdlive.ocd.user.model.Role;
import com.ocdlive.ocd.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Administration - Utilisateurs", description = "Endpoints de gestion des utilisateurs réservés aux administrateurs")
public class AdminUserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    @Operation(summary = "Lister tous les utilisateurs")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userMapper.toResponseList(userService.getAllUsers()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un utilisateur par son ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userMapper.toResponse(userService.getUserById(id)));
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Filtrer les utilisateurs par rôle")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable Role role) {
        return ResponseEntity.ok(userMapper.toResponseList(userService.getUsersByRole(role)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activer ou désactiver un compte utilisateur")
    public ResponseEntity<UserResponse> updateUserStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        return ResponseEntity.ok(userMapper.toResponse(userService.updateUserStatus(id, enabled)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un utilisateur")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
