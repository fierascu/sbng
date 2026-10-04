package eu.wee.sbng.config;

import eu.wee.sbng.TestOAuth2ClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeycloakRolesOidcUserServiceTest {

    private final ClientRegistration keycloak = new TestOAuth2ClientConfig()
            .clientRegistrationRepository().findByRegistrationId("keycloak");

    @Test
    void testAdminIsLoggedInWithAdminAuthority() {
        OidcUser user = load("sbng-admin", List.of("SBNG_ROLE_ADMIN", "offline_access", "uma_authorization"));

        assertThat(user.getName()).isEqualTo("sbng-admin");
        assertThat(user.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .contains("SBNG_ROLE_ADMIN")
                .doesNotContain("SBNG_ROLE_VIEW", "offline_access", "uma_authorization");
    }

    @Test
    void testViewerIsLoggedInWithViewAuthority() {
        OidcUser user = load("sbng-viewer", List.of("SBNG_ROLE_VIEW", "offline_access"));

        assertThat(user.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .contains("SBNG_ROLE_VIEW")
                .doesNotContain("SBNG_ROLE_ADMIN");
    }

    @Test
    void testUserWithOnlyKeycloakDefaultRolesIsRefused() {
        assertThatThrownBy(() -> load("sbng-norole", List.of("offline_access", "uma_authorization")))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .hasMessageContaining("sbng-norole");
    }

    @Test
    void testUserWithoutRolesClaimIsRefused() {
        assertThatThrownBy(() -> load("sbng-norole", null))
                .isInstanceOf(OAuth2AuthenticationException.class);
    }

    private OidcUser load(String username, List<String> roles) {
        OidcIdToken.Builder idToken = OidcIdToken.withTokenValue("id-token")
                .subject(username)
                .claim("preferred_username", username)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60));
        if (roles != null) {
            idToken.claim(KeycloakRolesOidcUserService.ROLES_CLAIM, roles);
        }
        OidcIdToken token = idToken.build();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                "access-token", Instant.now(), Instant.now().plusSeconds(60), Set.of("openid"));

        // stands in for OidcUserService, which would call Keycloak's userinfo endpoint
        KeycloakRolesOidcUserService service = new KeycloakRolesOidcUserService(
                request -> new DefaultOidcUser(Set.of(), request.getIdToken(), "preferred_username"));
        return service.loadUser(new OidcUserRequest(keycloak, accessToken, token, Map.of()));
    }
}
