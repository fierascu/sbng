package eu.wee.sbng;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // was httpBasic, see https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/index.html
    // now delegates authentication to Keycloak, see docker-compose.yml + keycloak/realm-export.json
    @Bean
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
                        // /services/** is unauthenticated by design (see authorizeHttpRequests below);
                        // a CSRF token would otherwise still be demanded on its POSTs, so exempt it too.
                        .ignoringRequestMatchers("/services/**"))
                // CookieCsrfTokenRepository only writes the cookie once the token is actually
                // read; force that read on every request so the cookie is present before the
                // SPA needs it for its first POST/PUT/DELETE.
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // /** after services implies any endpoint after this
                        .requestMatchers("/services/**").permitAll()
                        // browsers auto-request this; don't force Basic auth on it
                        .requestMatchers("/favicon.ico").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(Customizer.withDefaults());

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
