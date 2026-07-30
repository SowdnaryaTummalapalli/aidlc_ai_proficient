package url_shortener.ai_proficient.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "short_urls", indexes = @Index(name = "idx_short_urls_code", columnList = "short_code", unique = true))
public class ShortUrl {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "short_code", nullable = false, unique = true, length = 32)
    private String shortCode;
    @Column(name = "destination_url", nullable = false, length = 2048)
    private String destinationUrl;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "expires_at")
    private Instant expiresAt;
    @Version private long version;

    protected ShortUrl() { }

    public ShortUrl(String shortCode, String destinationUrl, Instant createdAt, Instant expiresAt) {
        this.shortCode = Objects.requireNonNull(shortCode);
        this.destinationUrl = Objects.requireNonNull(destinationUrl);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.expiresAt = expiresAt;
    }
    public boolean isExpired(Instant now) { return expiresAt != null && !expiresAt.isAfter(now); }
    public Long getId() { return id; }
    public String getShortCode() { return shortCode; }
    public String getDestinationUrl() { return destinationUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
}
