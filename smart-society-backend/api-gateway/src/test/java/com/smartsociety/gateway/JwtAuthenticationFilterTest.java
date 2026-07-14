package com.smartsociety.gateway;

import com.smartsociety.gateway.config.GatewayAppProperties;
import com.smartsociety.gateway.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    @Mock private GatewayJwtUtil      jwtUtil;
    @Mock private GatewayAppProperties gatewayProperties;
    @Mock private GatewayFilterChain  filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtFilter;

    @BeforeEach
    void setUp() {
        lenient().when(filterChain.filter(any())).thenReturn(Mono.empty());
    }

    @Nested
    @DisplayName("Public paths")
    class PublicPathTests {

        @Test
        @DisplayName("Should skip JWT validation for /api/auth/login")
        void publicPath_skipsJwtValidation() {
            when(gatewayProperties.getPublicPaths())
                    .thenReturn(List.of("/api/auth/login", "/api/auth/register"));

            MockServerHttpRequest request = MockServerHttpRequest
                    .post("/api/auth/login").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            StepVerifier.create(jwtFilter.filter(exchange, filterChain))
                    .verifyComplete();

            verify(jwtUtil, never()).isValid(any());
            verify(filterChain).filter(any());
        }
    }

    @Nested
    @DisplayName("Protected paths")
    class ProtectedPathTests {

        @BeforeEach
        void setUpProtected() {
            when(gatewayProperties.getPublicPaths()).thenReturn(List.of("/api/auth/login"));
        }

        @Test
        @DisplayName("Should reject request with no Authorization header — 401")
        void noAuthHeader_returns401() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/complaints").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            StepVerifier.create(jwtFilter.filter(exchange, filterChain))
                    .verifyComplete();

            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            verify(filterChain, never()).filter(any());
        }

        @Test
        @DisplayName("Should reject request with invalid Bearer token — 401")
        void invalidToken_returns401() {
            when(jwtUtil.isValid("bad.token")).thenReturn(false);

            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/complaints")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer bad.token")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            StepVerifier.create(jwtFilter.filter(exchange, filterChain))
                    .verifyComplete();

            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should pass request with valid token and inject X-User-Id header")
        void validToken_injectsUserHeaders() {
            String validToken = "valid.jwt.token";

            when(jwtUtil.isValid(validToken)).thenReturn(true);
            when(jwtUtil.getUserId(validToken)).thenReturn("user-uuid-123");
            when(jwtUtil.getRole(validToken)).thenReturn("RESIDENT");
            when(jwtUtil.getSocietyId(validToken)).thenReturn("society-uuid-456");
            when(jwtUtil.getEmail(validToken)).thenReturn("user@test.com");

            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/complaints")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            StepVerifier.create(jwtFilter.filter(exchange, filterChain))
                    .verifyComplete();

            verify(filterChain).filter(argThat(ex -> {
                HttpHeaders headers = ex.getRequest().getHeaders();
                return "user-uuid-123".equals(headers.getFirst("X-User-Id"))
                        && "RESIDENT".equals(headers.getFirst("X-User-Role"))
                        && "society-uuid-456".equals(headers.getFirst("X-Society-Id"));
            }));
        }

        @Test
        @DisplayName("Should reject Authorization header without Bearer prefix")
        void noBearerPrefix_returns401() {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/societies")
                    .header(HttpHeaders.AUTHORIZATION, "Basic somebase64value")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            StepVerifier.create(jwtFilter.filter(exchange, filterChain))
                    .verifyComplete();

            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            verify(jwtUtil, never()).isValid(any());
        }
    }
}