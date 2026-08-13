package eu.wee.sbng;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://custom.example.com")
class CorsConfigurablePropertyTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testAllowedOriginComesFromProperty() throws Exception {
        mockMvc.perform(options("/api/todos")
                        .header("Origin", "http://custom.example.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://custom.example.com"));
    }

    @Test
    void testDefaultOriginNoLongerAllowedWhenPropertyOverridden() throws Exception {
        mockMvc.perform(options("/api/todos")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

}
