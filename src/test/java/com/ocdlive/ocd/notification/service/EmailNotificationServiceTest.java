package com.ocdlive.ocd.notification.service;

import com.ocdlive.ocd.equipment.model.Equipment;
import com.ocdlive.ocd.equipment.model.EquipmentCategory;
import com.ocdlive.ocd.pack.model.Pack;
import com.ocdlive.ocd.payment.model.Payment;
import com.ocdlive.ocd.payment.model.PaymentMethod;
import com.ocdlive.ocd.payment.model.PaymentStatus;
import com.ocdlive.ocd.reservation.model.Reservation;
import com.ocdlive.ocd.reservation.model.ReservationLocationType;
import com.ocdlive.ocd.reservation.model.ReservationStatus;
import com.ocdlive.ocd.subscription.model.Subscription;
import com.ocdlive.ocd.subscription.model.SubscriptionStatus;
import com.ocdlive.ocd.user.model.Role;
import com.ocdlive.ocd.user.model.User;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - EmailNotificationService")
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    private Pack samplePack;
    private Reservation sampleReservation;
    private Payment samplePayment;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailNotificationService, "fromEmail", "noreply@ocdlive.com");
        ReflectionTestUtils.setField(emailNotificationService, "adminEmail", "admin@ocdlive.com");

        Equipment camera = Equipment.builder()
                .id(1L)
                .name("Caméra Sony FX3")
                .category(EquipmentCategory.VIDEO)
                .build();

        samplePack = Pack.builder()
                .id(10L)
                .name("Pack Live Premium")
                .price(new BigDecimal("150000.00"))
                .durationMinutes(90)
                .description("Diffusion multi-caméras et habillage pro")
                .equipments(List.of(camera))
                .build();

        sampleReservation = Reservation.builder()
                .id(50L)
                .fullNameOrCompany("Agence Digitale Nova")
                .email("client@nova-agency.com")
                .phoneNumber("+22890112233")
                .country("Togo")
                .locationType(ReservationLocationType.LOCAL)
                .activitySector("E-commerce & Mode")
                .activityDescription("Présentation de notre nouvelle collection")
                .appointmentDateTime(LocalDateTime.of(2026, 12, 15, 14, 30))
                .pack(samplePack)
                .status(ReservationStatus.CONFIRMED)
                .build();

        samplePayment = Payment.builder()
                .id(1L)
                .transactionReference("OCD-PAY-12345678")
                .amount(new BigDecimal("150000.00"))
                .currency("XOF")
                .method(PaymentMethod.MOBILE_MONEY)
                .status(PaymentStatus.SUCCESS)
                .reservation(sampleReservation)
                .build();
    }

    @Test
    @DisplayName("sendReservationPaymentConfirmationEmails() - Devrait envoyer 1 email au client et 1 email à l'admin")
    void shouldSendEmailsToClientAndAdminOnReservationPayment() throws Exception {
        given(mailSender.createMimeMessage())
                .willAnswer(inv -> new MimeMessage(Session.getInstance(new Properties())));

        emailNotificationService.sendReservationPaymentConfirmationEmails(samplePayment, sampleReservation);

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(2)).send(messageCaptor.capture());

        List<MimeMessage> sentMessages = messageCaptor.getAllValues();
        assertThat(sentMessages).hasSize(2);

        MimeMessage clientMessage = sentMessages.get(0);
        assertThat(clientMessage.getAllRecipients()[0].toString()).isEqualTo("client@nova-agency.com");
        assertThat(clientMessage.getSubject()).contains("Pack Live Premium");

        MimeMessage adminMessage = sentMessages.get(1);
        assertThat(adminMessage.getAllRecipients()[0].toString()).isEqualTo("admin@ocdlive.com");
        assertThat(adminMessage.getSubject()).contains("Agence Digitale Nova");
    }

    @Test
    @DisplayName("sendSubscriptionPaymentConfirmationEmails() - Devrait envoyer 1 email à l'abonné et 1 email à l'admin")
    void shouldSendEmailsToSubscriberAndAdminOnSubscriptionPayment() throws Exception {
        given(mailSender.createMimeMessage())
                .willAnswer(inv -> new MimeMessage(Session.getInstance(new Properties())));

        User client = User.builder()
                .id(2L)
                .firstName("Kofi")
                .lastName("Mensah")
                .email("kofi@cameroun.cm")
                .role(Role.ROLE_USER)
                .build();

        Subscription subscription = Subscription.builder()
                .id(70L)
                .client(client)
                .pack(samplePack)
                .country("Cameroun")
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 11, 13))
                .endDate(LocalDate.of(2026, 12, 13))
                .remainingLives(4)
                .build();

        emailNotificationService.sendSubscriptionPaymentConfirmationEmails(samplePayment, subscription);

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(2)).send(messageCaptor.capture());

        List<MimeMessage> sentMessages = messageCaptor.getAllValues();
        assertThat(sentMessages.get(0).getAllRecipients()[0].toString()).isEqualTo("kofi@cameroun.cm");
        assertThat(sentMessages.get(1).getAllRecipients()[0].toString()).isEqualTo("admin@ocdlive.com");
    }
}

