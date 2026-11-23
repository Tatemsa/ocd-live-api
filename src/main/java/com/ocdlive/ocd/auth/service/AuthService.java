package com.ocdlive.ocd.auth.service;

import com.ocdlive.ocd.auth.dto.AuthResponse;
import com.ocdlive.ocd.auth.dto.LoginRequest;
import com.ocdlive.ocd.auth.dto.RefreshTokenRequest;
import com.ocdlive.ocd.auth.dto.RegisterRequest;
import com.ocdlive.ocd.exception.ResourceAlreadyExistsException;
import com.ocdlive.ocd.exception.UnauthorizedException;
import com.ocdlive.ocd.security.jwt.JwtService;
import com.ocdlive.ocd.user.model.Role;
import com.ocdlive.ocd.user.model.User;
import com.ocdlive.ocd.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.security.jwt.expiration}")
    private Long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration:604800000}")
    private Long refreshExpiration;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("user", "email", request.getEmail());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        return buildAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        // Authentifie l'utilisateur via Spring Security (vérifie email et mot de passe encodé)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + request.getEmail()));

        if (!user.isEnabled()) {
            throw new BadCredentialsException("Ce compte utilisateur est désactivé.");
        }

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        final String refreshToken = request.getRefreshToken();
        final String userEmail;

        try {
            userEmail = jwtService.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new UnauthorizedException("Refresh token invalide ou expiré.");
        }

        if (userEmail == null) {
            throw new UnauthorizedException("Refresh token invalide.");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UnauthorizedException("Utilisateur introuvable pour ce refresh token."));

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Ce compte utilisateur est désactivé.");
        }

        boolean valid;
        try {
            valid = jwtService.isTokenValid(refreshToken, user);
        } catch (Exception e) {
            throw new UnauthorizedException("Refresh token expiré ou invalide.");
        }

        if (!valid) {
            throw new UnauthorizedException("Refresh token invalide.");
        }

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("role", user.getRole().name());

        String accessToken = jwtService.generateToken(extraClaims, user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .refreshExpiresIn(refreshExpiration)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }
}
