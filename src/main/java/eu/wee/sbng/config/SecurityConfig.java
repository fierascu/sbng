package eu.wee.sbng.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Keycloak realm roles (keycloak/realm-export.json) allowed into the oauth2Login part of the app.
    public static final String ROLE_ADMIN = "SBNG_ROLE_ADMIN";
    public static final String ROLE_VIEW = "SBNG_ROLE_VIEW";
    static final Set<String> APP_ROLES = Set.of(ROLE_ADMIN, ROLE_VIEW);

    // /api/tasks/** uses httpBasic instead of the Keycloak oauth2Login below - evaluated first
    // (lower @Order) so its securityMatcher carves those requests out of the oauth2Login chain.
    @Bean
    @Order(1)
    public SecurityFilterChain tasksSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/tasks/**")
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        // CsrfConfigurer defaults this to CsrfAuthenticationStrategy, which rotates
                        // (clears) the CSRF cookie on every authentication event. With httpBasic +
                        // STATELESS every request re-authenticates from scratch, so that rotation
                        // fires on every request and immediately invalidates the cookie the client
                        // just used. There's no login session here to protect from fixation, so skip it.
                        .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy()))
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(User.builder()
                .username("q")
                .password(encoder.encode("q"))
                .roles("USER")
                .build());
    }

    // was httpBasic, see https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/index.html
    // now delegates authentication to Keycloak, see docker-compose.yml + keycloak/realm-export.json
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // needed only when the SPA is served from a different origin than the API
                // (ng serve's proxy.conf.json keeps them same-origin in dev, so this mainly
                // matters for a production split-origin deployment).
                .cors(Customizer.withDefaults())
                // cookie-based repository matches Angular's default HttpClientXsrfModule,
                // which reads the XSRF-TOKEN cookie and echoes it back as the X-XSRF-TOKEN
                // header on state-changing requests.
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        // /api2/services/** and /services/** (the CXF SOAP endpoint) are
                        // unauthenticated by design (see authorizeHttpRequests below); a CSRF
                        // token would otherwise still be demanded on their POSTs, so exempt both -
                        // a SOAP client has no XSRF-TOKEN cookie/header to send anyway.
                        .ignoringRequestMatchers("/api2/services/**", "/services/**"))
                // CookieCsrfTokenRepository only writes the cookie once the token is actually
                // read; force that read on every request so the cookie is present before the
                // SPA needs it for its first POST/PUT/DELETE.
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // /** after services implies any endpoint after this
                        .requestMatchers("/api2/services/**").permitAll()
                        // CXF SOAP endpoint (see soap/CxfConfig) - a SOAP client can't follow an
                        // HTML/OAuth2 login redirect, and it only ever returns mocked demo data.
                        .requestMatchers("/services/**").permitAll()
                        // browsers auto-request this; don't force Basic auth on it
                        .requestMatchers("/favicon.ico").permitAll()
                        // Spring Security filters the ERROR dispatch too (not just the original
                        // REQUEST), so a 401 from the /api/tasks Basic chain gets forwarded here
                        // internally by Boot's error handling and would otherwise be re-challenged
                        // by oauth2Login below, replacing the Basic 401 with a Keycloak redirect.
                        .requestMatchers("/error").permitAll()
                        // KeycloakRolesOidcUserService already refuses the login of a user with
                        // none of these roles; checked here too so access never rests on that alone.
                        .anyRequest().hasAnyAuthority(ROLE_ADMIN, ROLE_VIEW))
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(new KeycloakRolesOidcUserService())))

                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; " +
                                                "script-src 'self'; " +
                                                "style-src 'self' 'unsafe-inline'; " +
                                                "img-src 'self' data:; " +
                                                "font-src 'self' data:; " +
                                                "connect-src 'self'; " +
                                                "object-src 'none'; " +
                                                "base-uri 'self'; " +
                                                "frame-ancestors 'none'"
                                ).reportOnly()
                        )
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:4200}") List<String> allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private static final class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            csrfToken.getToken();

            filterChain.doFilter(request, response);
        }
    }

}
