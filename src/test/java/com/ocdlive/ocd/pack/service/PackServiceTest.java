package com.ocdlive.ocd.pack.service;

import com.ocdlive.ocd.equipment.model.Equipment;
import com.ocdlive.ocd.equipment.model.EquipmentCategory;
import com.ocdlive.ocd.equipment.repository.EquipmentRepository;
import com.ocdlive.ocd.exception.BadRequestException;
import com.ocdlive.ocd.exception.ResourceAlreadyExistsException;
import com.ocdlive.ocd.exception.ResourceNotFoundException;
import com.ocdlive.ocd.pack.dto.PackRequest;
import com.ocdlive.ocd.pack.dto.PackResponse;
import com.ocdlive.ocd.pack.mapper.PackMapper;
import com.ocdlive.ocd.pack.model.Pack;
import com.ocdlive.ocd.pack.repository.PackRepository;
import com.ocdlive.ocd.user.model.Role;
import com.ocdlive.ocd.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - PackService")
class PackServiceTest {

    @Mock
    private PackRepository packRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private PackMapper packMapper;

    @InjectMocks
    private PackService packService;

    private User ownerUser;
    private User adminUser;
    private User otherUser;
    private Equipment equipment1;
    private Pack samplePack;
    private PackRequest sampleRequest;
    private PackResponse sampleResponse;

    @BeforeEach
    void setUp() {
        ownerUser = User.builder()
                .id(1L)
                .email("owner@ocdlive.com")
                .firstName("Owner")
                .lastName("User")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        adminUser = User.builder()
                .id(2L)
                .email("admin@ocdlive.com")
                .firstName("Super")
                .lastName("Admin")
                .role(Role.ROLE_ADMIN)
                .enabled(true)
                .build();

        otherUser = User.builder()
                .id(3L)
                .email("other@ocdlive.com")
                .firstName("Other")
                .lastName("User")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        equipment1 = Equipment.builder()
                .id(10L)
                .name("Caméra Sony FX3")
                .category(EquipmentCategory.VIDEO)
                .quantity(2)
                .available(true)
                .build();

        samplePack = Pack.builder()
                .id(100L)
                .name("Pack Live Premium")
                .price(new BigDecimal("150000.00"))
                .durationMinutes(90)
                .description("Pack complet multi-caméras")
                .equipments(List.of(equipment1))
                .active(true)
                .createdBy(ownerUser)
                .build();

        sampleRequest = PackRequest.builder()
                .name("Pack Live Premium")
                .price(new BigDecimal("150000.00"))
                .durationMinutes(90)
                .description("Pack complet multi-caméras")
                .equipmentIds(List.of(10L))
                .active(true)
                .build();

        sampleResponse = PackResponse.builder()
                .id(100L)
                .name("Pack Live Premium")
                .price(new BigDecimal("150000.00"))
                .durationMinutes(90)
                .description("Pack complet multi-caméras")
                .active(true)
                .createdBy("Owner User")
                .build();
    }

    @Nested
    @DisplayName("createPack()")
    class CreatePackTests {

        @Test
        @DisplayName("Devrait créer un pack avec ses équipements quand l'utilisateur est authentifié")
        void shouldCreatePackWhenUserIsAuthenticated() {
            given(packRepository.existsByNameIgnoreCase("Pack Live Premium")).willReturn(false);
            given(packMapper.toEntity(sampleRequest)).willReturn(samplePack);
            given(equipmentRepository.findAllById(List.of(10L))).willReturn(List.of(equipment1));
            given(packRepository.save(samplePack)).willReturn(samplePack);
            given(packMapper.toResponse(samplePack)).willReturn(sampleResponse);

            PackResponse result = packService.createPack(sampleRequest, ownerUser);

            assertThat(result).isEqualTo(sampleResponse);
            assertThat(samplePack.getCreatedBy()).isEqualTo(ownerUser);
            assertThat(samplePack.getEquipments()).containsExactly(equipment1);
            verify(packRepository).save(samplePack);
        }

        @Test
        @DisplayName("Devrait créer un pack sans équipements si equipmentIds est null ou vide")
        void shouldCreatePackWithEmptyEquipmentIds() {
            sampleRequest.setEquipmentIds(null);
            given(packRepository.existsByNameIgnoreCase("Pack Live Premium")).willReturn(false);
            given(packMapper.toEntity(sampleRequest)).willReturn(samplePack);
            given(packRepository.save(samplePack)).willReturn(samplePack);
            given(packMapper.toResponse(samplePack)).willReturn(sampleResponse);

            PackResponse result = packService.createPack(sampleRequest, ownerUser);

            assertThat(result).isEqualTo(sampleResponse);
            assertThat(samplePack.getEquipments()).isEmpty();
            verify(equipmentRepository, never()).findAllById(any());
        }

        @Test
        @DisplayName("Devrait lever AccessDeniedException si l'utilisateur courant est null")
        void shouldThrowAccessDeniedExceptionWhenUserIsNull() {
            assertThatThrownBy(() -> packService.createPack(sampleRequest, null))
                    .isInstanceOf(AccessDeniedException.class);

            verify(packRepository, never()).save(any());
        }

        @Test
        @DisplayName("Devrait lever ResourceAlreadyExistsException si le nom du pack existe déjà")
        void shouldThrowResourceAlreadyExistsExceptionWhenNameExists() {
            given(packRepository.existsByNameIgnoreCase("Pack Live Premium")).willReturn(true);

            assertThatThrownBy(() -> packService.createPack(sampleRequest, ownerUser))
                    .isInstanceOf(ResourceAlreadyExistsException.class);

            verify(packRepository, never()).save(any());
        }

        @Test
        @DisplayName("Devrait lever BadRequestException si certains IDs d'équipements sont introuvables")
        void shouldThrowBadRequestExceptionWhenSomeEquipmentsNotFound() {
            sampleRequest.setEquipmentIds(List.of(10L, 999L));
            given(packRepository.existsByNameIgnoreCase("Pack Live Premium")).willReturn(false);
            given(packMapper.toEntity(sampleRequest)).willReturn(samplePack);
            given(equipmentRepository.findAllById(List.of(10L, 999L))).willReturn(List.of(equipment1));

            assertThatThrownBy(() -> packService.createPack(sampleRequest, ownerUser))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("introuvables");

            verify(packRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updatePack()")
    class UpdatePackTests {

        @Test
        @DisplayName("Devrait permettre au propriétaire de modifier son pack")
        void shouldAllowOwnerToUpdatePack() {
            given(packRepository.findById(100L)).willReturn(Optional.of(samplePack));
            given(equipmentRepository.findAllById(List.of(10L))).willReturn(List.of(equipment1));
            given(packRepository.save(samplePack)).willReturn(samplePack);
            given(packMapper.toResponse(samplePack)).willReturn(sampleResponse);

            PackResponse result = packService.updatePack(100L, sampleRequest, ownerUser);

            assertThat(result).isEqualTo(sampleResponse);
            verify(packMapper).updateEntityFromRequest(sampleRequest, samplePack);
            verify(packRepository).save(samplePack);
        }

        @Test
        @DisplayName("Devrait permettre à un administrateur de modifier le pack d'un autre utilisateur")
        void shouldAllowAdminToUpdateAnyPack() {
            given(packRepository.findById(100L)).willReturn(Optional.of(samplePack));
            given(equipmentRepository.findAllById(List.of(10L))).willReturn(List.of(equipment1));
            given(packRepository.save(samplePack)).willReturn(samplePack);
            given(packMapper.toResponse(samplePack)).willReturn(sampleResponse);

            PackResponse result = packService.updatePack(100L, sampleRequest, adminUser);

            assertThat(result).isEqualTo(sampleResponse);
        }

        @Test
        @DisplayName("Devrait lever AccessDeniedException si un autre utilisateur non-admin tente de modifier le pack")
        void shouldThrowAccessDeniedExceptionWhenNonOwnerUpdatesPack() {
            given(packRepository.findById(100L)).willReturn(Optional.of(samplePack));

            assertThatThrownBy(() -> packService.updatePack(100L, sampleRequest, otherUser))
                    .isInstanceOf(AccessDeniedException.class);

            verify(packRepository, never()).save(any());
        }

        @Test
        @DisplayName("Devrait lever ResourceAlreadyExistsException si le nouveau nom est déjà pris")
        void shouldThrowResourceAlreadyExistsExceptionWhenNewNameExists() {
            PackRequest renameRequest = PackRequest.builder()
                    .name("Pack Existant")
                    .price(new BigDecimal("100000.00"))
                    .durationMinutes(60)
                    .build();

            given(packRepository.findById(100L)).willReturn(Optional.of(samplePack));
            given(packRepository.existsByNameIgnoreCase("Pack Existant")).willReturn(true);

            assertThatThrownBy(() -> packService.updatePack(100L, renameRequest, ownerUser))
                    .isInstanceOf(ResourceAlreadyExistsException.class);
        }

        @Test
        @DisplayName("Devrait lever ResourceNotFoundException si le pack n'existe pas")
        void shouldThrowResourceNotFoundExceptionWhenPackDoesNotExist() {
            given(packRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> packService.updatePack(999L, sampleRequest, ownerUser))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("togglePackStatus() & deletePack()")
    class StatusAndDeleteTests {

        @Test
        @DisplayName("togglePackStatus() - Devrait activer/désactiver le pack pour le propriétaire ou admin")
        void shouldTogglePackStatusForAdmin() {
            given(packRepository.findById(100L)).willReturn(Optional.of(samplePack));
            given(packRepository.save(samplePack)).willReturn(samplePack);
            sampleResponse.setActive(false);
            given(packMapper.toResponse(samplePack)).willReturn(sampleResponse);

            PackResponse result = packService.togglePackStatus(100L, false, adminUser);

            assertThat(samplePack.isActive()).isFalse();
            assertThat(result.isActive()).isFalse();
        }

        @Test
        @DisplayName("deletePack() - Devrait supprimer le pack si l'utilisateur est propriétaire")
        void shouldDeletePackWhenOwner() {
            given(packRepository.findById(100L)).willReturn(Optional.of(samplePack));

            packService.deletePack(100L, ownerUser);

            verify(packRepository).delete(samplePack);
        }

        @Test
        @DisplayName("deletePack() - Devrait lever AccessDeniedException si l'utilisateur n'est ni propriétaire ni admin")
        void shouldThrowAccessDeniedWhenNonOwnerDeletesPack() {
            given(packRepository.findById(100L)).willReturn(Optional.of(samplePack));

            assertThatThrownBy(() -> packService.deletePack(100L, otherUser))
                    .isInstanceOf(AccessDeniedException.class);

            verify(packRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Consultation des packs")
    class ReadPackTests {

        @Test
        @DisplayName("getMyPacks() - Devrait retourner les packs de l'utilisateur connecté")
        void shouldReturnMyPacks() {
            given(packRepository.findByCreatedById(1L)).willReturn(List.of(samplePack));
            given(packMapper.toResponseList(List.of(samplePack))).willReturn(List.of(sampleResponse));

            List<PackResponse> result = packService.getMyPacks(ownerUser);

            assertThat(result).hasSize(1).containsExactly(sampleResponse);
        }

        @Test
        @DisplayName("getPacksByUser() - Devrait retourner les packs actifs d'un utilisateur")
        void shouldReturnActivePacksByUser() {
            given(packRepository.findByCreatedByIdAndActiveTrue(1L)).willReturn(List.of(samplePack));
            given(packMapper.toResponseList(List.of(samplePack))).willReturn(List.of(sampleResponse));

            List<PackResponse> result = packService.getPacksByUser(1L);

            assertThat(result).hasSize(1).containsExactly(sampleResponse);
        }

        @Test
        @DisplayName("getActivePacks() - Devrait retourner tous les packs actifs")
        void shouldReturnActivePacks() {
            given(packRepository.findByActiveTrue()).willReturn(List.of(samplePack));
            given(packMapper.toResponseList(List.of(samplePack))).willReturn(List.of(sampleResponse));

            List<PackResponse> result = packService.getActivePacks();

            assertThat(result).hasSize(1).containsExactly(sampleResponse);
        }

        @Test
        @DisplayName("getAllPacks() - Devrait retourner tous les packs (actifs et inactifs)")
        void shouldReturnAllPacks() {
            given(packRepository.findAll()).willReturn(List.of(samplePack));
            given(packMapper.toResponseList(List.of(samplePack))).willReturn(List.of(sampleResponse));

            List<PackResponse> result = packService.getAllPacks();

            assertThat(result).hasSize(1).containsExactly(sampleResponse);
        }

        @Test
        @DisplayName("getPackById() - Devrait retourner le pack par son ID")
        void shouldReturnPackById() {
            given(packRepository.findById(100L)).willReturn(Optional.of(samplePack));
            given(packMapper.toResponse(samplePack)).willReturn(sampleResponse);

            PackResponse result = packService.getPackById(100L);

            assertThat(result).isEqualTo(sampleResponse);
        }

        @Test
        @DisplayName("getPackById() - Devrait lever ResourceNotFoundException si l'ID est introuvable")
        void shouldThrowNotFoundWhenPackDoesNotExist() {
            given(packRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> packService.getPackById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}

