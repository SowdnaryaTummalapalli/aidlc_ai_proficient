package url_shortener.ai_proficient.web;

import java.net.URI;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import url_shortener.ai_proficient.domain.ShortUrl;
import url_shortener.ai_proficient.service.UrlService;

@RestController
public class RedirectController {
    private final UrlService urls;
    public RedirectController(UrlService urls) { this.urls = urls; }
    @GetMapping("/{shortCode:[A-Za-z0-9_-]+}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        ShortUrl url = urls.resolveAndRecordClick(shortCode);
        return ResponseEntity.status(302).location(URI.create(url.getDestinationUrl()))
                .header(HttpHeaders.CACHE_CONTROL, CacheControl.noStore().getHeaderValue()).build();
    }
}
