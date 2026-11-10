package com.ocdlive.ocd.user.controller;

import com.ocdlive.ocd.user.dto.UserResponse;
import com.ocdlive.ocd.user.mapper.UserMapper;
import com.ocdlive.ocd.user.model.Role;
import com.ocdlive.ocd.user.model.User;
import com.ocdlive.ocd.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - AdminUserController")
class AdminUserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AdminUserController adminUserController;

    private User sampleUser;
    private UserResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("user@ocdlive.com")
                .firstName("Jean")
                .lastName("Dupont")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        sampleResponse = UserResponse.builder()
                .id(1L)
                .email("user@ocdlive.com")
                .firstName("Jean")
                .lastName("Dupont")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("GET /admin/users - Devrait lister tous les utilisateurs")
    void shouldGetAllUsers() {
        given(userService.getAllUsers()).willReturn(List.of(sampleUser));
        given(userMapper.toResponseList(List.of(sampleUser))).willReturn(List.of(sampleResponse));

        ResponseEntity<List<UserResponse>> response = adminUserController.getAllUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1).containsExactly(sampleResponse);
    }

    @Test
    @DisplayName("GET /admin/users/{id} - Devrait retourner l'utilisateur par son ID")
    void shouldGetUserById() {
        given(userService.getUserById(1L)).willReturn(sampleUser);
        given(userMapper.toResponse(sampleUser)).willReturn(sampleResponse);

        ResponseEntity<UserResponse> response = adminUserController.getUserById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(sampleResponse);
    }

    @Test
    @DisplayName("GET /admin/users/role/{role} - Devrait filtrer les utilisateurs par rôle")
    void shouldGetUsersByRole() {
        given(userService.getUsersByRole(Role.ROLE_USER)).willReturn(List.of(sampleUser));
        given(userMapper.toResponseList(List.of(sampleUser))).willReturn(List.of(sampleResponse));

        ResponseEntity<List<UserResponse>> response = adminUserController.getUsersByRole(Role.ROLE_USER);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1).containsExactly(sampleResponse);
    }

    @Test
    @DisplayName("PATCH /admin/users/{id}/status - Devrait activer ou désactiver un compte utilisateur")
    void shouldUpdateUserStatus() {
        sampleUser.setEnabled(false);
        sampleResponse.setEnabled(false);
        given(userService.updateUserStatus(1L, false)).willReturn(sampleUser);
        given(userMapper.toResponse(sampleUser)).willReturn(sampleResponse);

        ResponseEntity<UserResponse> response = adminUserController.updateUserStatus(1L, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isEnabled()).isFalse();
    }

    @Test
    @DisplayName("DELETE /admin/users/{id} - Devrait supprimer l'utilisateur et retourner 204 No Content")
    void shouldDeleteUser() {
        ResponseEntity<Void> response = adminUserController.deleteUser(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userService).deleteUser(1L);
    }
}

