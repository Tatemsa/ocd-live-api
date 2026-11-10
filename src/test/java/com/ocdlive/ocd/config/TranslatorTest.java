package com.ocdlive.ocd.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - Translator")
class TranslatorTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private Translator translator;

    @Test
    @DisplayName("toLocale() - Devrait retourner le message traduit quand la clé existe")
    void shouldReturnTranslatedMessageWhenKeyExists() {
        given(messageSource.getMessage(eq("error.unauthorized"), any(), any(Locale.class)))
                .willReturn("Authentification requise.");

        String result = translator.toLocale("error.unauthorized");

        assertThat(result).isEqualTo("Authentification requise.");
    }

    @Test
    @DisplayName("toLocale() - Devrait retourner le code du message si la clé est introuvable")
    void shouldReturnMessageCodeWhenNoSuchMessageExceptionThrown() {
        given(messageSource.getMessage(eq("unknown.key"), any(), any(Locale.class)))
                .willThrow(new NoSuchMessageException("unknown.key"));

        String result = translator.toLocale("unknown.key");

        assertThat(result).isEqualTo("unknown.key");
    }

    @Test
    @DisplayName("toLocaleWithDefault() - Devrait retourner la traduction ou le message par défaut")
    void shouldReturnTranslatedMessageWithDefault() {
        given(messageSource.getMessage(eq("error.forbidden"), any(), eq("Message par défaut"), any(Locale.class)))
                .willReturn("Accès refusé.");

        String result = translator.toLocaleWithDefault("error.forbidden", "Message par défaut");

        assertThat(result).isEqualTo("Accès refusé.");
    }
}

