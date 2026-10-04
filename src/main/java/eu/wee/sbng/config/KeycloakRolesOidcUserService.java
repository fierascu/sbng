package eu.wee.sbng.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Turns the Keycloak realm roles (put in the ID token / userinfo as the "roles" claim by the
 * protocol mapper in keycloak/realm-export.json) into Spring authorities, and refuses the login
 * outright for a user holding none of the app roles - so a Keycloak user without SBNG_ROLE_ADMIN
 * or SBNG_ROLE_VIEW ends up on /login?error instead of with a session that 403s everywhere.
 */
public class KeycloakRolesOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    static final String ROLES_CLAIM = "roles";

    private final OAuth2UserService<OidcUserRequest, OidcUser> delegate;

    public KeycloakRolesOidcUserService() {
        this(new OidcUserService());
    }

    KeycloakRolesOidcUserService(OAuth2UserService<OidcUserRequest, OidcUser> delegate) {
        this.delegate = delegate;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser user = delegate.loadUser(userRequest);

        // Keycloak also emits its built-in default roles (offline_access, uma_authorization, ...)
        // in the same claim; only the app's own roles become authorities.
        List<String> roles = user.getClaimAsStringList(ROLES_CLAIM);
        Set<GrantedAuthority> appRoles = new HashSet<>();
        if (roles != null) {
            roles.stream()
                    .filter(SecurityConfig.APP_ROLES::contains)
                    .map(SimpleGrantedAuthority::new)
                    .forEach(appRoles::add);
        }
        if (appRoles.isEmpty()) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.ACCESS_DENIED,
                    "User '" + user.getPreferredUsername() + "' has no sbng role", null));
        }

        Set<GrantedAuthority> authorities = new HashSet<>(user.getAuthorities());
        authorities.addAll(appRoles);
        String nameAttribute = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();
        return new DefaultOidcUser(authorities, user.getIdToken(), user.getUserInfo(), nameAttribute);
    }
}
