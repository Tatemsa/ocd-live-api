package com.ocdlive.ocd.auth.service;

import com.ocdlive.ocd.auth.dto.AuthResponse;
import com.ocdlive.ocd.auth.dto.LoginRequest;
import com.ocdlive.ocd.auth.dto.RegisterRequest;
import com.ocdlive.ocd.exception.ResourceAlreadyExistsException;
import com.ocdlive.ocd.security.jwt.JwtService;
import com.ocdlive.ocd.user.model.Role;
import com.ocdlive.ocd.user.model.User;
import com.ocdlive.ocd.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - AuthService")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private static final Long JWT_EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "jwtExpiration", JWT_EXPIRATION);
    }

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("Devrait inscrire un nouvel utilisateur et retourner un AuthResponse avec JWT")
        @SuppressWarnings("unchecked")
        void shouldRegisterNewUserAndReturnAuthResponse() {
            RegisterRequest request = RegisterRequest.builder()
                    .firstName("Kofi")
                    .lastName("Mensah")
                    .email("kofi@entreprise.tg")
                    .password("secret123")
                    .build();

            given(userRepository.existsByEmail("kofi@entreprise.tg")).willReturn(false);
            given(passwordEncoder.encode("secret123")).willReturn("encodedSecret");
            given(userRepository.save(any(User.class))).willAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(10L);
                return u;
            });
            given(jwtService.generateToken(any(Map.class), any(User.class))).willReturn("registered.jwt.token");

            AuthResponse response = authService.register(request);

            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("registered.jwt.token");
            assertThat(response.getUserId()).isEqualTo(10L);
            assertThat(response.getEmail()).isEqualTo("kofi@entreprise.tg");
            assertThat(response.getRole()).isEqualTo(Role.ROLE_USER);
        }

        @Test
        @DisplayName("Devrait lever ResourceAlreadyExistsException si l'email est déjà utilisé")
        void shouldThrowExceptionWhenRegisterEmailAlreadyExists() {
            RegisterRequest request = RegisterRequest.builder()
                    .firstName("Kofi")
                    .lastName("Mensah")
                    .email("existing@entreprise.tg")
                    .password("secret123")
                    .build();

            given(userRepository.existsByEmail("existing@entreprise.tg")).willReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(ResourceAlreadyExistsException.class);

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("Devrait authentifier l'utilisateur actif et retourner un AuthResponse avec token JWT")
        @SuppressWarnings("unchecked")
        void shouldAuthenticateActiveUserAndReturnAuthResponse() {
            LoginRequest request = LoginRequest.builder()
                    .email("admin@ocdlive.com")
                    .password("admin123")
                    .build();

            User user = User.builder()
                    .id(1L)
                    .email("admin@ocdlive.com")
                    .password("encodedPassword")
                    .firstName("Super")
                    .lastName("Admin")
                    .role(Role.ROLE_ADMIN)
                    .enabled(true)
                    .build();

            given(userRepository.findByEmail("admin@ocdlive.com")).willReturn(Optional.of(user));
            given(jwtService.generateToken(any(Map.class), eq(user))).willReturn("mocked.jwt.token");

            AuthResponse response = authService.login(request);

            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("mocked.jwt.token");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getExpiresIn()).isEqualTo(JWT_EXPIRATION);
            assertThat(response.getUserId()).isEqualTo(1L);
            assertThat(response.getEmail()).isEqualTo("admin@ocdlive.com");
            assertThat(response.getFirstName()).isEqualTo("Super");
            assertThat(response.getLastName()).isEqualTo("Admin");
            assertThat(response.getRole()).isEqualTo(Role.ROLE_ADMIN);

            verify(authenticationManager).authenticate(
                    new UsernamePasswordAuthenticationToken("admin@ocdlive.com", "admin123")
            );

            ArgumentCaptor<Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(Map.class);
            verify(jwtService).generateToken(claimsCaptor.capture(), eq(user));
            assertThat(claimsCaptor.getValue())
                    .containsEntry("userId", 1L)
                    .containsEntry("role", "ROLE_ADMIN");
        }

        @Test
        @DisplayName("Devrait lever BadCredentialsException si l'AuthenticationManager échoue")
        void shouldThrowBadCredentialsExceptionWhenAuthenticationManagerFails() {
            LoginRequest request = LoginRequest.builder()
                    .email("admin@ocdlive.com")
                    .password("wrongPassword")
                    .build();

            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);

            verify(userRepository, never()).findByEmail(any());
            verify(jwtService, never()).generateToken(any(), any());
        }

        @Test
        @DisplayName("Devrait lever UsernameNotFoundException si l'utilisateur n'existe pas en base")
        void shouldThrowUsernameNotFoundExceptionWhenUserNotFound() {
            LoginRequest request = LoginRequest.builder()
                    .email("unknown@ocdlive.com")
                    .password("password")
                    .build();

            given(userRepository.findByEmail("unknown@ocdlive.com")).willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("unknown@ocdlive.com");

            verify(jwtService, never()).generateToken(any(), any());
        }

        @Test
        @DisplayName("Devrait lever BadCredentialsException si le compte utilisateur est désactivé")
        void shouldThrowBadCredentialsExceptionWhenUserIsDisabled() {
            LoginRequest request = LoginRequest.builder()
                    .email("disabled@ocdlive.com")
                    .password("password")
                    .build();

            User disabledUser = User.builder()
                    .id(2L)
                    .email("disabled@ocdlive.com")
                    .firstName("Disabled")
                    .lastName("User")
                    .role(Role.ROLE_USER)
                    .enabled(false)
                    .build();

            given(userRepository.findByEmail("disabled@ocdlive.com")).willReturn(Optional.of(disabledUser));

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("désactivé");

            verify(jwtService, never()).generateToken(any(), any());
        }
    }
}
