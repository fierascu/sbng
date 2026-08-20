package eu.wee.sbng;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

/**
 * Mirrors the "keycloak" registration from application.properties, but with every endpoint
 * given explicitly so building it never makes a network call to a (possibly not running)
 * Keycloak container — {@code @WebMvcTest} slices don't load OAuth2ClientAutoConfiguration,
 * so this is what supplies the ClientRegistrationRepository oauth2Login() needs to build.
 */
@TestConfiguration
public class TestOAuth2ClientConfig {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration keycloak = ClientRegistration.withRegistrationId("keycloak")
                .clientId("sbng-app")
                .clientSecret("sbng-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri("http://localhost:9080/realms/sbng/protocol/openid-connect/auth")
                .tokenUri("http://localhost:9080/realms/sbng/protocol/openid-connect/token")
                .jwkSetUri("http://localhost:9080/realms/sbng/protocol/openid-connect/certs")
                .userInfoUri("http://localhost:9080/realms/sbng/protocol/openid-connect/userinfo")
                .userNameAttributeName("preferred_username")
                .clientName("Keycloak")
                .build();
        return new InMemoryClientRegistrationRepository(keycloak);
    }
}
