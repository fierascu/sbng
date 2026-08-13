package eu.wee.sbng;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@Import(SecurityConfig.class)
class TodosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void clearTodos() {
        ((List<?>) ReflectionTestUtils.getField(TodosController.class, "TODOS")).clear();
    }

    @Test
    void testTodosController401() throws Exception {
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isUnauthorized())
                .andDo(print())
                .andExpect(content().string(""));
    }

    @Test
    void testTodosController200() throws Exception {
        mockMvc.perform(get("/api/todos").with(httpBasic("q", "q")))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string("[]"));
    }

    @Test
    void testApiTodosGetAndPostAndGet() throws Exception {
        MvcResult getResult = mockMvc.perform(get("/api/todos").with(httpBasic("q", "q")))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string("[]"))
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andReturn();

        Cookie xsrfCookie = getResult.getResponse().getCookie("XSRF-TOKEN");

        mockMvc.perform(post("/api/todos/{todo}", "todo1")
                        .content("")
                        .with(httpBasic("q", "q"))
                        .cookie(xsrfCookie)
                        .header("X-XSRF-TOKEN", xsrfCookie.getValue()))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string("[\"todo1\"]"));

        mockMvc.perform(get("/api/todos").with(httpBasic("q", "q")))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string("[\"todo1\"]"));
    }

    @Test
    void testApiTodosGetAndPostAndGetSameSession() throws Exception {
        MockHttpSession session = new MockHttpSession();

        MvcResult getResult = mockMvc.perform(get("/api/todos").with(httpBasic("q", "q")).session(session))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string("[]"))
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andReturn();

        Cookie xsrfCookie = getResult.getResponse().getCookie("XSRF-TOKEN");

        MvcResult postResult = mockMvc.perform(post("/api/todos/{todo}", "todo1")
                        .content("")
                        .with(httpBasic("q", "q"))
                        .session(session)
                        .cookie(xsrfCookie)
                        .header("X-XSRF-TOKEN", xsrfCookie.getValue()))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string("[\"todo1\"]"))
                .andReturn();

        // the CSRF cookie must survive being used, not get rotated/invalidated by the Basic-Auth
        // re-authentication on every request (regression check for the token-rotation bug)
        assertThat(postResult.getResponse().getCookie("XSRF-TOKEN")).isNull();

        mockMvc.perform(get("/api/todos").with(httpBasic("q", "q")).session(session).cookie(xsrfCookie))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string("[\"todo1\"]"));
    }

    @Test
    void testCorsPreflightAllowsConfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/todos")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void testCorsPreflightRejectsUnknownOrigin() throws Exception {
        mockMvc.perform(options("/api/todos")
                        .header("Origin", "http://evil.example.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

}