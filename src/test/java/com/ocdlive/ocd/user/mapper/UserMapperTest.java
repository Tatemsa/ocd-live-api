package com.ocdlive.ocd.user.mapper;

import com.ocdlive.ocd.user.dto.UserResponse;
import com.ocdlive.ocd.user.model.Role;
import com.ocdlive.ocd.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests unitaires - UserMapper")
class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    @DisplayName("toResponse() - Devrait convertir une entité User en UserResponse")
    void shouldMapUserToUserResponse() {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .id(1L)
                .email("admin@ocdlive.com")
                .firstName("Super")
                .lastName("Admin")
                .role(Role.ROLE_ADMIN)
                .enabled(true)
                .createdAt(now)
                .build();

        UserResponse response = userMapper.toResponse(user);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("admin@ocdlive.com");
        assertThat(response.getFirstName()).isEqualTo("Super");
        assertThat(response.getLastName()).isEqualTo("Admin");
        assertThat(response.getRole()).isEqualTo(Role.ROLE_ADMIN);
        assertThat(response.isEnabled()).isTrue();
        assertThat(response.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("toResponseList() - Devrait convertir une liste d'utilisateurs en liste de UserResponse")
    void shouldMapUserListToUserResponseList() {
        User user = User.builder()
                .id(2L)
                .email("user@ocdlive.com")
                .firstName("Jean")
                .lastName("Dupont")
                .role(Role.ROLE_USER)
                .enabled(false)
                .build();

        List<UserResponse> responses = userMapper.toResponseList(List.of(user));

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getEmail()).isEqualTo("user@ocdlive.com");
        assertThat(responses.get(0).isEnabled()).isFalse();
    }
}

