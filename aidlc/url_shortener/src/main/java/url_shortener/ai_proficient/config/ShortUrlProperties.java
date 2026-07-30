package url_shortener.ai_proficient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.short-url")
public record ShortUrlProperties(String baseUrl, int codeLength) {
}
