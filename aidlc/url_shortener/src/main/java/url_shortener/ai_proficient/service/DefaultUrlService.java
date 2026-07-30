package url_shortener.ai_proficient.service;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import url_shortener.ai_proficient.domain.ClickEvent;
import url_shortener.ai_proficient.domain.ShortUrl;
import url_shortener.ai_proficient.repository.ClickEventRepository;
import url_shortener.ai_proficient.repository.ShortUrlRepository;
import url_shortener.ai_proficient.web.CreateShortUrlRequest;
import url_shortener.ai_proficient.web.UrlAnalyticsResponse;

@Service
public class DefaultUrlService implements UrlService {
    private static final int MAX_GENERATION_ATTEMPTS = 5;
    private final ShortUrlRepository shortUrls;
    private final ClickEventRepository clickEvents;
    private final ShortCodeGenerator codeGenerator;
    private final Clock clock;
    public DefaultUrlService(ShortUrlRepository shortUrls, ClickEventRepository clickEvents, ShortCodeGenerator codeGenerator, Clock clock) {
        this.shortUrls = shortUrls; this.clickEvents = clickEvents; this.codeGenerator = codeGenerator; this.clock = clock;
    }
    @Override @Transactional
    public ShortUrl create(CreateShortUrlRequest request) {
        validateDestination(request.destinationUrl());
        Instant now = clock.instant();
        if (request.expiresAt() != null && !request.expiresAt().isAfter(now)) throw new InvalidUrlException("expiresAt must be in the future");
        if (request.customCode() != null) return save(request.customCode(), request.destinationUrl(), now, request.expiresAt());
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            try { return save(codeGenerator.nextCode(), request.destinationUrl(), now, request.expiresAt()); }
            catch (ShortCodeConflictException ignored) { /* collision; retry with a fresh secure code */ }
        }
        throw new IllegalStateException("Unable to allocate a unique short code; retry the request");
    }
    @Override @Transactional
    public ShortUrl resolveAndRecordClick(String shortCode) {
        ShortUrl url = findActive(shortCode);
        clickEvents.save(new ClickEvent(url, clock.instant()));
        return url;
    }
    @Override @Transactional(readOnly = true)
    public UrlAnalyticsResponse analytics(String shortCode) {
        ShortUrl url = findActive(shortCode);
        Instant now = clock.instant();
        Instant from = now.minusSeconds(30L * 24 * 60 * 60);
        Map<LocalDate, Long> daily = new TreeMap<>();
        clickEvents.findAllInRange(url.getId(), from, now.plusMillis(1)).forEach(event ->
                daily.merge(event.getClickedAt().atZone(ZoneOffset.UTC).toLocalDate(), 1L, Long::sum));
        return new UrlAnalyticsResponse(url.getShortCode(), url.getDestinationUrl(), url.getCreatedAt(), url.getExpiresAt(), clickEvents.countByShortUrl_Id(url.getId()), daily);
    }
    private ShortUrl save(String code, String destination, Instant now, Instant expiry) {
        if (shortUrls.existsByShortCode(code)) throw new ShortCodeConflictException("Short code is already in use");
        try { return shortUrls.saveAndFlush(new ShortUrl(code, destination, now, expiry)); }
        catch (DataIntegrityViolationException exception) { throw new ShortCodeConflictException("Short code is already in use"); }
    }
    private ShortUrl findActive(String code) {
        ShortUrl url = shortUrls.findByShortCode(code).orElseThrow(() -> new ShortUrlNotFoundException(code));
        if (url.isExpired(clock.instant())) throw new ShortUrlNotFoundException(code);
        return url;
    }
    private void validateDestination(String value) {
        try {
            URI uri = URI.create(value);
            if (!uri.isAbsolute() || uri.getHost() == null || !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) throw new InvalidUrlException("destinationUrl must be an absolute HTTP(S) URL");
        } catch (IllegalArgumentException exception) { throw new InvalidUrlException("destinationUrl must be a valid URL"); }
    }
}
