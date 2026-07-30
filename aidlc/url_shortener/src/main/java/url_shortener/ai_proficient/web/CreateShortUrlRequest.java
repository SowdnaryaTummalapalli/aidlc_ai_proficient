package url_shortener.ai_proficient.web;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateShortUrlRequest(
        @NotBlank @Size(max = 2048) String destinationUrl,
        @Pattern(regexp = "[A-Za-z0-9_-]{4,32}", message = "customCode must contain 4-32 letters, numbers, underscores, or hyphens") String customCode,
        Instant expiresAt) { }
