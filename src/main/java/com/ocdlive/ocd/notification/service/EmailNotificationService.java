package com.ocdlive.ocd.notification.service;

import com.ocdlive.ocd.equipment.model.Equipment;
import com.ocdlive.ocd.pack.model.Pack;
import com.ocdlive.ocd.payment.model.Payment;
import com.ocdlive.ocd.reservation.model.Reservation;
import com.ocdlive.ocd.reservation.model.ReservationLocationType;
import com.ocdlive.ocd.subscription.model.Subscription;
import com.ocdlive.ocd.user.model.User;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final JavaMailSender mailSender;

    @Value("${application.mail.from:noreply@ocdlive.com}")
    private String fromEmail;

    @Value("${application.mail.admin-email:admin@ocdlive.com}")
    private String adminEmail;

    /**
     * Envoie deux emails lors de la confirmation par paiement d'une réservation de Live :
     * 1. Au client consommateur avec le récapitulatif du paiement, du pack réservé et de son rendez-vous
     * 2. À l'administrateur avec toutes les informations du client, du pack et du rendez-vous
     */
    public void sendReservationPaymentConfirmationEmails(Payment payment, Reservation reservation) {
        if (reservation == null || reservation.getPack() == null) {
            return;
        }
        Pack pack = reservation.getPack();

        // 1. Email au consommateur (client)
        String clientSubject = "Confirmation de paiement et de votre rendez-vous Live - " + pack.getName();
        String clientHtml = buildClientReservationEmailHtml(payment, reservation, pack);
        sendHtmlEmail(reservation.getEmail(), clientSubject, clientHtml);

        // 2. Email à l'administrateur
        String adminSubject = "[ADMIN] Nouveau rendez-vous Live payé et confirmé - " + reservation.getFullNameOrCompany();
        String adminHtml = buildAdminReservationEmailHtml(payment, reservation, pack);
        sendHtmlEmail(adminEmail, adminSubject, adminHtml);
    }

    /**
     * Envoie deux emails lors de la validation par paiement d'un abonnement mensuel à un pack :
     * 1. Au client abonné avec les détails de son pack mensuel et de sa période de validité
     * 2. À l'administrateur avec les détails du client et du pack souscrit
     */
    public void sendSubscriptionPaymentConfirmationEmails(Payment payment, Subscription subscription) {
        if (subscription == null || subscription.getPack() == null || subscription.getClient() == null) {
            return;
        }
        Pack pack = subscription.getPack();
        User client = subscription.getClient();

        // 1. Email au client abonné
        String clientSubject = "Activation de votre abonnement mensuel OCD Live - " + pack.getName();
        String clientHtml = buildClientSubscriptionEmailHtml(payment, subscription, pack, client);
        sendHtmlEmail(client.getEmail(), clientSubject, clientHtml);

        // 2. Email à l'administrateur
        String adminSubject = "[ADMIN] Nouvel abonnement mensuel payé - " + client.getFirstName() + " " + client.getLastName();
        String adminHtml = buildAdminSubscriptionEmailHtml(payment, subscription, pack, client);
        sendHtmlEmail(adminEmail, adminSubject, adminHtml);
    }

    private String buildClientReservationEmailHtml(Payment payment, Reservation reservation, Pack pack) {
        String locationLabel = reservation.getLocationType() == ReservationLocationType.LOCAL
                ? "En présentiel (Studio au Togo)"
                : "En ligne (Streaming à distance)";

        String appointmentDateStr = reservation.getAppointmentDateTime() != null
                ? reservation.getAppointmentDateTime().format(DATE_TIME_FORMATTER)
                : "Non spécifiée";

        String equipmentsSummary = formatEquipments(pack);
        String txRef = payment != null ? payment.getTransactionReference() : "ABONNEMENT-MENSUEL";
        String amountStr = payment != null
                ? payment.getAmount() + " " + payment.getCurrency()
                : pack.getPrice() + " (Inclus dans l'abonnement)";

        return """
                <div style="font-family: Arial, sans-serif; color: #1f2937; max-width: 640px; margin: 0 auto; padding: 24px; border: 1px solid #e5e7eb; border-radius: 8px;">
                    <h2 style="color: #111827; margin-top: 0;">Bonjour %s,</h2>
                    <p>Votre paiement a bien été reçu et votre rendez-vous pour votre session <strong>OCD Live</strong> est désormais <strong>CONFIRMÉ</strong>.</p>
                    
                    <h3 style="color: #2563eb; border-bottom: 1px solid #e5e7eb; padding-bottom: 6px;">Détails de votre Pack réservé</h3>
                    <ul>
                        <li><strong>Nom du Pack :</strong> %s</li>
                        <li><strong>Prix du Pack :</strong> %s</li>
                        <li><strong>Durée du Live :</strong> %d minutes</li>
                        <li><strong>Description & Avantages :</strong> %s</li>
                        <li><strong>Équipements inclus :</strong> %s</li>
                    </ul>

                    <h3 style="color: #2563eb; border-bottom: 1px solid #e5e7eb; padding-bottom: 6px;">Détails de votre Rendez-vous Live</h3>
                    <ul>
                        <li><strong>Date et heure :</strong> %s</li>
                        <li><strong>Modalité :</strong> %s</li>
                        <li><strong>Catégorie / Secteur :</strong> %s</li>
                        <li><strong>Pays :</strong> %s</li>
                    </ul>

                    <h3 style="color: #2563eb; border-bottom: 1px solid #e5e7eb; padding-bottom: 6px;">Récapitulatif du Paiement</h3>
                    <ul>
                        <li><strong>Référence de transaction :</strong> %s</li>
                        <li><strong>Montant réglé :</strong> %s</li>
                    </ul>

                    <p style="margin-top: 24px; font-size: 14px; color: #6b7280;">Merci pour votre confiance — L'équipe OCD Live.</p>
                </div>
                """.formatted(
                reservation.getFullNameOrCompany(),
                pack.getName(),
                pack.getPrice(),
                pack.getDurationMinutes() != null ? pack.getDurationMinutes() : 60,
                pack.getDescription() != null ? pack.getDescription() : "Standard",
                equipmentsSummary,
                appointmentDateStr,
                locationLabel,
                reservation.getActivitySector(),
                reservation.getCountry(),
                txRef,
                amountStr
        );
    }

    private String buildAdminReservationEmailHtml(Payment payment, Reservation reservation, Pack pack) {
        String locationLabel = reservation.getLocationType() == ReservationLocationType.LOCAL
                ? "En présentiel (Directement au Togo)"
                : "En ligne";

        String appointmentDateStr = reservation.getAppointmentDateTime() != null
                ? reservation.getAppointmentDateTime().format(DATE_TIME_FORMATTER)
                : "Non spécifiée";

        String txRef = payment != null ? payment.getTransactionReference() : "ABONNEMENT-MENSUEL";
        String amountStr = payment != null
                ? payment.getAmount() + " " + payment.getCurrency() + " (" + payment.getMethod() + ")"
                : "Couvert par abonnement mensuel";

        return """
                <div style="font-family: Arial, sans-serif; color: #1f2937; max-width: 640px; margin: 0 auto; padding: 24px; border: 1px solid #e5e7eb; border-radius: 8px;">
                    <h2 style="color: #dc2626; margin-top: 0;">[ADMIN] Nouvelle Réservation Live Confirmée</h2>
                    <p>Un client vient de confirmer et payer un rendez-vous pour un live de présentation business.</p>

                    <h3 style="color: #111827; border-bottom: 1px solid #e5e7eb; padding-bottom: 6px;">Informations du Client & Business</h3>
                    <ul>
                        <li><strong>Client / Entreprise / Marque :</strong> %s</li>
                        <li><strong>Email :</strong> %s</li>
                        <li><strong>Téléphone :</strong> %s</li>
                        <li><strong>Pays :</strong> %s</li>
                        <li><strong>Catégorie de Business :</strong> %s</li>
                        <li><strong>Description du projet :</strong> %s</li>
                    </ul>

                    <h3 style="color: #111827; border-bottom: 1px solid #e5e7eb; padding-bottom: 6px;">Informations du Créneau & Modalité</h3>
                    <ul>
                        <li><strong>Date et heure du Live :</strong> %s</li>
                        <li><strong>Lieu :</strong> %s</li>
                    </ul>

                    <h3 style="color: #111827; border-bottom: 1px solid #e5e7eb; padding-bottom: 6px;">Informations du Pack & Paiement</h3>
                    <ul>
                        <li><strong>Pack réservé :</strong> %s (ID: %d)</li>
                        <li><strong>Prix catalogue :</strong> %s</li>
                        <li><strong>Durée prévue :</strong> %d minutes</li>
                        <li><strong>Description du pack :</strong> %s</li>
                        <li><strong>Référence paiement :</strong> %s</li>
                        <li><strong>Montant encaissé :</strong> %s</li>
                    </ul>
                </div>
                """.formatted(
                reservation.getFullNameOrCompany(),
                reservation.getEmail(),
                reservation.getPhoneNumber(),
                reservation.getCountry(),
                reservation.getActivitySector(),
                reservation.getActivityDescription() != null ? reservation.getActivityDescription() : "Aucune description fournie",
                appointmentDateStr,
                locationLabel,
                pack.getName(),
                pack.getId() != null ? pack.getId() : 0L,
                pack.getPrice(),
                pack.getDurationMinutes() != null ? pack.getDurationMinutes() : 60,
                pack.getDescription() != null ? pack.getDescription() : "N/A",
                txRef,
                amountStr
        );
    }

    private String buildClientSubscriptionEmailHtml(Payment payment, Subscription subscription, Pack pack, User client) {
        String startStr = subscription.getStartDate() != null ? subscription.getStartDate().format(DATE_FORMATTER) : "-";
        String endStr = subscription.getEndDate() != null ? subscription.getEndDate().format(DATE_FORMATTER) : "-";

        return """
                <div style="font-family: Arial, sans-serif; color: #1f2937; max-width: 640px; margin: 0 auto; padding: 24px; border: 1px solid #e5e7eb; border-radius: 8px;">
                    <h2 style="color: #111827; margin-top: 0;">Bonjour %s %s,</h2>
                    <p>Votre paiement a été validé et votre abonnement mensuel au pack <strong>%s</strong> est désormais <strong>ACTIF</strong>.</p>
                    <ul>
                        <li><strong>Pack :</strong> %s</li>
                        <li><strong>Description & Avantages :</strong> %s</li>
                        <li><strong>Durée par session Live :</strong> %d minutes</li>
                        <li><strong>Quota mensuel de Lives :</strong> %d sessions</li>
                        <li><strong>Pays rattaché :</strong> %s</li>
                        <li><strong>Période de validité :</strong> du %s au %s</li>
                        <li><strong>Référence transaction :</strong> %s (%s %s)</li>
                    </ul>
                </div>
                """.formatted(
                client.getFirstName(),
                client.getLastName(),
                pack.getName(),
                pack.getName(),
                pack.getDescription() != null ? pack.getDescription() : "Pack Abonnement Mensuel",
                pack.getDurationMinutes() != null ? pack.getDurationMinutes() : 60,
                subscription.getRemainingLives() != null ? subscription.getRemainingLives() : 4,
                subscription.getCountry(),
                startStr,
                endStr,
                payment.getTransactionReference(),
                payment.getAmount(),
                payment.getCurrency()
        );
    }

    private String buildAdminSubscriptionEmailHtml(Payment payment, Subscription subscription, Pack pack, User client) {
        return """
                <div style="font-family: Arial, sans-serif; color: #1f2937; max-width: 640px; margin: 0 auto; padding: 24px; border: 1px solid #e5e7eb; border-radius: 8px;">
                    <h2 style="color: #dc2626; margin-top: 0;">[ADMIN] Nouvel Abonnement Mensuel Activé</h2>
                    <ul>
                        <li><strong>Client :</strong> %s %s (%s)</li>
                        <li><strong>Pays :</strong> %s</li>
                        <li><strong>Pack souscrit :</strong> %s</li>
                        <li><strong>Quota mensuel :</strong> %d lives (%d min / live)</li>
                        <li><strong>Montant payé :</strong> %s %s (Réf: %s)</li>
                    </ul>
                </div>
                """.formatted(
                client.getFirstName(),
                client.getLastName(),
                client.getEmail(),
                subscription.getCountry(),
                pack.getName(),
                subscription.getRemainingLives() != null ? subscription.getRemainingLives() : 4,
                pack.getDurationMinutes() != null ? pack.getDurationMinutes() : 60,
                payment.getAmount(),
                payment.getCurrency(),
                payment.getTransactionReference()
        );
    }

    private String formatEquipments(Pack pack) {
        if (pack.getEquipments() == null || pack.getEquipments().isEmpty()) {
            return "Configuration studio standard";
        }
        return pack.getEquipments().stream()
                .map(Equipment::getName)
                .collect(Collectors.joining(", "));
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Email envoyé avec succès à '{}' (Sujet: '{}')", to, subject);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email à '{}' (Sujet: '{}') : {}", to, subject, e.getMessage());
        }
    }
}

