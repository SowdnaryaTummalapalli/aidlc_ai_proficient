package url_shortener.ai_proficient.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import url_shortener.ai_proficient.domain.ClickEvent;
import url_shortener.ai_proficient.domain.ShortUrl;
import url_shortener.ai_proficient.repository.ClickEventRepository;
import url_shortener.ai_proficient.repository.ShortUrlRepository;
import url_shortener.ai_proficient.web.CreateShortUrlRequest;
import url_shortener.ai_proficient.web.UrlAnalyticsResponse;

@ExtendWith(MockitoExtension.class)
class DefaultUrlServiceTest {
    private static final Instant NOW = Instant.parse("2026-07-30T10:15:30Z");

    @Mock private ShortUrlRepository shortUrls;
    @Mock private ClickEventRepository clickEvents;
    @Mock private ShortCodeGenerator codeGenerator;
    private DefaultUrlService service;

    @BeforeEach
    void setUp() {
        service = new DefaultUrlService(shortUrls, clickEvents, codeGenerator, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsGeneratedUrlWhenCodeIsAvailable() {
        when(codeGenerator.nextCode()).thenReturn("aB12Cd34");
        when(shortUrls.existsByShortCode("aB12Cd34")).thenReturn(false);
        when(shortUrls.saveAndFlush(any(ShortUrl.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShortUrl result = service.create(new CreateShortUrlRequest("https://example.com/docs", null, null));

        assertEquals("aB12Cd34", result.getShortCode());
        assertEquals("https://example.com/docs", result.getDestinationUrl());
        assertEquals(NOW, result.getCreatedAt());
        verify(shortUrls).saveAndFlush(any(ShortUrl.class));
    }

    @Test
    void retriesWhenGeneratedCodeCollides() {
        when(codeGenerator.nextCode()).thenReturn("duplicate", "unique123");
        when(shortUrls.existsByShortCode("duplicate")).thenReturn(true);
        when(shortUrls.existsByShortCode("unique123")).thenReturn(false);
        when(shortUrls.saveAndFlush(any(ShortUrl.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShortUrl result = service.create(new CreateShortUrlRequest("https://example.com", null, null));

        assertEquals("unique123", result.getShortCode());
        verify(codeGenerator, times(2)).nextCode();
    }

    @Test
    void rejectsUnsafeOrMalformedDestination() {
        CreateShortUrlRequest request = new CreateShortUrlRequest("javascript:alert(1)", null, null);

        assertThrows(InvalidUrlException.class, () -> service.create(request));
        verify(shortUrls, never()).saveAndFlush(any());
    }

    @Test
    void rejectsExpiryInThePast() {
        CreateShortUrlRequest request = new CreateShortUrlRequest("https://example.com", null, NOW.minusSeconds(1));

        assertThrows(InvalidUrlException.class, () -> service.create(request));
        verify(shortUrls, never()).saveAndFlush(any());
    }

    @Test
    void recordsClickForActiveLink() {
        ShortUrl link = new ShortUrl("guide2026", "https://example.com/docs", NOW.minusSeconds(60), null);
        when(shortUrls.findByShortCode("guide2026")).thenReturn(Optional.of(link));

        ShortUrl result = service.resolveAndRecordClick("guide2026");

        assertEquals(link, result);
        verify(clickEvents).save(any(ClickEvent.class));
    }

    @Test
    void doesNotRecordClickForExpiredLink() {
        ShortUrl expired = new ShortUrl("oldlink1", "https://example.com", NOW.minusSeconds(120), NOW.minusSeconds(1));
        when(shortUrls.findByShortCode("oldlink1")).thenReturn(Optional.of(expired));

        assertThrows(ShortUrlNotFoundException.class, () -> service.resolveAndRecordClick("oldlink1"));
        verify(clickEvents, never()).save(any());
    }

    @Test
    void returnsTotalAndDailyAnalyticsForLastThirtyDays() {
        ShortUrl link = new ShortUrl("metrics1", "https://example.com", NOW.minusSeconds(60), null);
        ClickEvent yesterday = new ClickEvent(link, NOW.minusSeconds(24 * 60 * 60));
        ClickEvent today = new ClickEvent(link, NOW.minusSeconds(10));
        when(shortUrls.findByShortCode("metrics1")).thenReturn(Optional.of(link));
        when(clickEvents.findAllInRange(eq(null), any(Instant.class), any(Instant.class))).thenReturn(List.of(yesterday, today));
        when(clickEvents.countByShortUrl_Id(null)).thenReturn(8L);

        UrlAnalyticsResponse result = service.analytics("metrics1");

        assertEquals(8L, result.totalClicks());
        assertEquals(1L, result.dailyClicksLast30Days().get(LocalDate.of(2026, 7, 29)));
        assertEquals(1L, result.dailyClicksLast30Days().get(LocalDate.of(2026, 7, 30)));
    }
}
