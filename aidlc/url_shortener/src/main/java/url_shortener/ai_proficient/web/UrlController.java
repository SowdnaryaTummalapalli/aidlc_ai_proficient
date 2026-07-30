package url_shortener.ai_proficient.web;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import url_shortener.ai_proficient.config.ShortUrlProperties;
import url_shortener.ai_proficient.domain.ShortUrl;
import url_shortener.ai_proficient.service.UrlService;

@RestController
@RequestMapping("/api/v1/urls")
public class UrlController {
    private final UrlService urls;
    private final ShortUrlProperties properties;
    public UrlController(UrlService urls, ShortUrlProperties properties) { this.urls = urls; this.properties = properties; }
    @PostMapping
    public ResponseEntity<ShortUrlResponse> create(@Valid @RequestBody CreateShortUrlRequest request) {
        ShortUrl created = urls.create(request);
        return ResponseEntity.created(URI.create(shortUrl(created.getShortCode()))).body(toResponse(created));
    }
    @GetMapping("/{shortCode}/analytics")
    public UrlAnalyticsResponse analytics(@PathVariable String shortCode) { return urls.analytics(shortCode); }
    private ShortUrlResponse toResponse(ShortUrl url) { return new ShortUrlResponse(url.getShortCode(), shortUrl(url.getShortCode()), url.getDestinationUrl(), url.getCreatedAt(), url.getExpiresAt()); }
    private String shortUrl(String code) { return properties.baseUrl().replaceAll("/+$", "") + "/" + code; }
}
