package url_shortener.ai_proficient.web;

import java.time.Instant;

public record ShortUrlResponse(String shortCode, String shortUrl, String destinationUrl, Instant createdAt, Instant expiresAt) { }
