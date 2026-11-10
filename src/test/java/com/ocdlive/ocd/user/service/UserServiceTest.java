package com.ocdlive.ocd.user.service;

import com.ocdlive.ocd.exception.ResourceAlreadyExistsException;
import com.ocdlive.ocd.exception.ResourceNotFoundException;
import com.ocdlive.ocd.user.model.Role;
import com.ocdlive.ocd.user.model.User;
import com.ocdlive.ocd.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("user@ocdlive.com")
                .password("rawPassword")
                .firstName("Jean")
                .lastName("Dupont")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();
    }

    @Nested
    @DisplayName("loadUserByUsername()")
    class LoadUserByUsernameTests {

        @Test
        @DisplayName("Devrait retourner UserDetails quand l'email existe")
        void shouldReturnUserDetailsWhenEmailExists() {
            given(userRepository.findByEmail("user@ocdlive.com")).willReturn(Optional.of(sampleUser));

            UserDetails result = userService.loadUserByUsername("user@ocdlive.com");

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("user@ocdlive.com");
            assertThat(result.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        }

        @Test
        @DisplayName("Devrait lever UsernameNotFoundException quand l'email n'existe pas")
        void shouldThrowUsernameNotFoundExceptionWhenEmailDoesNotExist() {
            given(userRepository.findByEmail("missing@ocdlive.com")).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.loadUserByUsername("missing@ocdlive.com"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("missing@ocdlive.com");
        }
    }

    @Nested
    @DisplayName("Consultation des utilisateurs")
    class ReadUserTests {

        @Test
        @DisplayName("getAllUsers() - Devrait retourner la liste complète des utilisateurs")
        void shouldReturnAllUsers() {
            given(userRepository.findAll()).willReturn(List.of(sampleUser));

            List<User> users = userService.getAllUsers();

            assertThat(users).hasSize(1).containsExactly(sampleUser);
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("getUserById() - Devrait retourner l'utilisateur s'il existe")
        void shouldReturnUserByIdWhenExists() {
            given(userRepository.findById(1L)).willReturn(Optional.of(sampleUser));

            User found = userService.getUserById(1L);

            assertThat(found).isEqualTo(sampleUser);
        }

        @Test
        @DisplayName("getUserById() - Devrait lever ResourceNotFoundException si l'ID est introuvable")
        void shouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
            given(userRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("getUserByEmail() - Devrait retourner l'utilisateur si l'email existe")
        void shouldReturnUserByEmailWhenExists() {
            given(userRepository.findByEmail("user@ocdlive.com")).willReturn(Optional.of(sampleUser));

            User found = userService.getUserByEmail("user@ocdlive.com");

            assertThat(found).isEqualTo(sampleUser);
        }

        @Test
        @DisplayName("getUserByEmail() - Devrait lever ResourceNotFoundException si l'email est introuvable")
        void shouldThrowResourceNotFoundExceptionWhenEmailDoesNotExist() {
            given(userRepository.findByEmail("unknown@ocdlive.com")).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserByEmail("unknown@ocdlive.com"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("unknown@ocdlive.com");
        }

        @Test
        @DisplayName("getUsersByRole() - Devrait filtrer les utilisateurs par rôle")
        void shouldReturnUsersByRole() {
            given(userRepository.findByRole(Role.ROLE_USER)).willReturn(List.of(sampleUser));

            List<User> result = userService.getUsersByRole(Role.ROLE_USER);

            assertThat(result).hasSize(1).containsExactly(sampleUser);
            verify(userRepository).findByRole(Role.ROLE_USER);
        }
    }

    @Nested
    @DisplayName("Création, modification et suppression d'utilisateur")
    class WriteUserTests {

        @Test
        @DisplayName("createUser() - Devrait encoder le mot de passe et sauvegarder le nouvel utilisateur")
        void shouldEncodePasswordAndSaveNewUser() {
            given(userRepository.existsByEmail("user@ocdlive.com")).willReturn(false);
            given(passwordEncoder.encode("rawPassword")).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

            User created = userService.createUser(sampleUser, passwordEncoder);

            assertThat(created.getPassword()).isEqualTo("encodedPassword");
            verify(passwordEncoder).encode("rawPassword");
            verify(userRepository).save(sampleUser);
        }

        @Test
        @DisplayName("createUser() - Devrait lever ResourceAlreadyExistsException si l'email est déjà utilisé")
        void shouldThrowResourceAlreadyExistsExceptionWhenEmailExists() {
            given(userRepository.existsByEmail("user@ocdlive.com")).willReturn(true);

            assertThatThrownBy(() -> userService.createUser(sampleUser, passwordEncoder))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("user@ocdlive.com");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("updateUserStatus() - Devrait mettre à jour le statut enabled de l'utilisateur")
        void shouldUpdateUserEnabledStatus() {
            given(userRepository.findById(1L)).willReturn(Optional.of(sampleUser));
            given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

            User updated = userService.updateUserStatus(1L, false);

            assertThat(updated.isEnabled()).isFalse();
            verify(userRepository).save(sampleUser);
        }

        @Test
        @DisplayName("deleteUser() - Devrait supprimer l'utilisateur s'il existe")
        void shouldDeleteUserWhenExists() {
            given(userRepository.existsById(1L)).willReturn(true);

            userService.deleteUser(1L);

            verify(userRepository).deleteById(1L);
        }

        @Test
        @DisplayName("deleteUser() - Devrait lever ResourceNotFoundException si l'utilisateur à supprimer n'existe pas")
        void shouldThrowResourceNotFoundExceptionWhenDeletingNonExistentUser() {
            given(userRepository.existsById(99L)).willReturn(false);

            assertThatThrownBy(() -> userService.deleteUser(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(userRepository, never()).deleteById(any());
        }
    }
}

