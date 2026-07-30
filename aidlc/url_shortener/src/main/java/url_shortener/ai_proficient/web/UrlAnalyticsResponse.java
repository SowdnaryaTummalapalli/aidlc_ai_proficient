package url_shortener.ai_proficient.web;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public record UrlAnalyticsResponse(String shortCode, String destinationUrl, Instant createdAt, Instant expiresAt, long totalClicks, Map<LocalDate, Long> dailyClicksLast30Days) { }
