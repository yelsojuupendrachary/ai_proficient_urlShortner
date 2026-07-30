package ai_pro_url_shortener;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AiProApplicationTests {
    @Autowired private MockMvc mockMvc;

    @Test
    void createsRedirectsAndReportsAnalytics() throws Exception {
        String response = mockMvc.perform(post("/api/urls").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/products?id=42\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.code").isString())
                .andReturn().getResponse().getContentAsString();
        String code = response.replaceAll(".*\\\"code\\\":\\\"([A-Za-z0-9]{8})\\\".*", "$1");
        mockMvc.perform(get("/" + code)).andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/products?id=42"));
        mockMvc.perform(get("/api/urls/" + code + "/analytics")).andExpect(status().isOk())
                .andExpect(jsonPath("$.clickCount", is(1)));
    }

    @Test
    void rejectsUnsupportedDestinationScheme() throws Exception {
        mockMvc.perform(post("/api/urls").contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"javascript:alert(1)\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForUnknownCode() throws Exception {
        mockMvc.perform(get("/api/urls/ZZZZZZZZ/analytics")).andExpect(status().isNotFound());
    }
}
