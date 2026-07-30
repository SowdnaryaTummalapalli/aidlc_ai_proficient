package url_shortener.ai_proficient;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UrlShortenerIntegrationTest {
    @Autowired private MockMvc mockMvc;

    @Test
    void createsRedirectsAndReportsAnalytics() throws Exception {
        mockMvc.perform(post("/api/v1/urls").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"destinationUrl\":\"https://example.com/docs\",\"customCode\":\"guide2026\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.shortCode").value("guide2026"));
        mockMvc.perform(get("/guide2026")).andExpect(status().isFound()).andExpect(header().string("Location", "https://example.com/docs"));
        mockMvc.perform(get("/api/v1/urls/guide2026/analytics"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalClicks").value(1));
    }

    @Test
    void rejectsUnsupportedDestinationScheme() throws Exception {
        mockMvc.perform(post("/api/v1/urls").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"destinationUrl\":\"javascript:alert(1)\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsConflictForDuplicateCustomCode() throws Exception {
        String request = "{\"destinationUrl\":\"https://example.com\",\"customCode\":\"fixedcode\"}";
        mockMvc.perform(post("/api/v1/urls").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/urls").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isConflict());
    }
}
