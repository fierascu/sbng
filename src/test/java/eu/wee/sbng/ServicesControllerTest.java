package eu.wee.sbng;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({SecurityConfig.class, TestOAuth2ClientConfig.class})
class ServicesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testServicesController200() throws Exception {
        mockMvc.perform(get("/api2/services"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string("CXF_API_OK"));
    }

    @Test
    void testServicesController200Post() throws Exception {
        mockMvc.perform(post("/api2/services/{service}", "service12")
                        .content(""))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string("[\"service12\"]"));
    }

}