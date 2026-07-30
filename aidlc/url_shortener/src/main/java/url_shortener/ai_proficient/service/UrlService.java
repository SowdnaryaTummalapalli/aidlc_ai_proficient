package url_shortener.ai_proficient.service;

import java.time.Instant;
import url_shortener.ai_proficient.domain.ShortUrl;
import url_shortener.ai_proficient.web.CreateShortUrlRequest;
import url_shortener.ai_proficient.web.UrlAnalyticsResponse;

public interface UrlService {
    ShortUrl create(CreateShortUrlRequest request);
    ShortUrl resolveAndRecordClick(String shortCode);
    UrlAnalyticsResponse analytics(String shortCode);
}
