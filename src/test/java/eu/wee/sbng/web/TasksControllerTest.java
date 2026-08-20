package eu.wee.sbng.web;

import eu.wee.sbng.TestOAuth2ClientConfig;
import eu.wee.sbng.config.SecurityConfig;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@Import({SecurityConfig.class, TestOAuth2ClientConfig.class})
class TasksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TasksController tasksController;

    @BeforeEach
    void clearTasks() {
        ((InMemoryList) ReflectionTestUtils.getField(tasksController, "tasks")).clear();
    }

    @Test
    void testTasksControllerRejectsAnonymous() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testTasksController200() throws Exception {
        mockMvc.perform(get("/api/tasks").with(httpBasic("q", "q")))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string("[]"));
    }

    @Test
    void testApiTasksGetAndPostAndGet() throws Exception {
        MvcResult getResult = mockMvc.perform(get("/api/tasks").with(httpBasic("q", "q")))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string("[]"))
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andReturn();

        Cookie xsrfCookie = getResult.getResponse().getCookie("XSRF-TOKEN");

        mockMvc.perform(post("/api/tasks/{task}", "task1")
                        .content("")
                        .with(httpBasic("q", "q"))
                        .cookie(xsrfCookie)
                        .header("X-XSRF-TOKEN", xsrfCookie.getValue()))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string("[\"task1\"]"));

        mockMvc.perform(get("/api/tasks").with(httpBasic("q", "q")))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string("[\"task1\"]"));
    }

}
