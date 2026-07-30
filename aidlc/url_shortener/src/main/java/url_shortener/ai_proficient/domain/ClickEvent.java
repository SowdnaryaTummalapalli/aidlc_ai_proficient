package url_shortener.ai_proficient.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "click_events", indexes = @Index(name = "idx_click_events_url_time", columnList = "short_url_id,clicked_at"))
public class ClickEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "short_url_id", nullable = false, foreignKey = @ForeignKey(name = "fk_click_events_short_url"))
    private ShortUrl shortUrl;
    @Column(name = "clicked_at", nullable = false)
    private Instant clickedAt;
    protected ClickEvent() { }
    public ClickEvent(ShortUrl shortUrl, Instant clickedAt) { this.shortUrl = shortUrl; this.clickedAt = clickedAt; }
    public Instant getClickedAt() { return clickedAt; }
}
