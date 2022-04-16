package org.devgraft.auth.api;

import org.devgraft.auth.service.SpyAuthService;
import org.devgraft.auth.service.TokenGenerateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthApiTest {
    private WebTestClient webTestClient;
    private SpyAuthService authService;

    @BeforeEach
    void setUp() {
        authService = new SpyAuthService();
        webTestClient = WebTestClient.bindToController(new AuthApi(authService))
                .configureClient()
                .build();
    }

    @Test
    void issueToken_createHttpStatus() {
        webTestClient.post()
                .uri("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue("{\"roles\": [\"ROLE_TEST\"], \"data\": {\"id\":\"id\"}, \"validity\": 100000, \"refreshValidity\": 1000000}"))
                .exchange()
                .expectStatus()
                .isCreated();
    }

    @Test
    void issueToken_returnValue() {
        webTestClient.post()
                .uri("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue("{\"roles\": [\"ROLE_TEST\"], \"data\": {\"id\":\"id\"}, \"validity\": 100000, \"refreshValidity\": 1000000}"))
                .exchange()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("accessToken")
                .jsonPath("$.refreshToken").isEqualTo("refreshToken");
    }

    @Test
    void issueToken_passesRequestToAuthService() {
        Map<String, Object> givenData = new HashMap<>();
        givenData.put("id", "id");
        long givenValidity = 100000L;
        long givenRefreshValidity = 1000000L;
        String givenRole = "ROLE_TEST";
        TokenGenerateRequest givenRequest = new TokenGenerateRequest(List.of(givenRole), givenData, givenValidity, givenRefreshValidity);

        webTestClient.post()
                .uri("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(givenRequest))
                .exchange();

        assertThat(authService.generateToken_argument.getRoles()).isNotEmpty();
        assertThat(authService.generateToken_argument.getRoles().get(0)).isEqualTo(givenRole);
        assertThat(authService.generateToken_argument.getValidity()).isEqualTo(givenValidity);
        assertThat(authService.generateToken_argument.getRefreshValidity()).isEqualTo(givenRefreshValidity);
        assertThat(authService.generateToken_argument.getData()).isEqualTo(givenData);
    }

    @Test
    void breakToken_okHttpStatus() {
        webTestClient.delete()
                .uri("/auth/{token}", "token")
                .exchange()
                .expectStatus()
                .isOk();
    }
}