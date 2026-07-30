package ai_pro_url_shortener.url;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "short_urls")
public class ShortUrl {
    @Id
    @Column(nullable = false, updatable = false, length = 16)
    private String code;

    @Column(nullable = false, length = 2048)
    private String destinationUrl;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant expiresAt;

    @Column(nullable = false)
    private long clickCount;

    protected ShortUrl() { }

    public ShortUrl(String code, String destinationUrl, Instant createdAt, Instant expiresAt) {
        this.code = code;
        this.destinationUrl = destinationUrl;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public void registerClick() { clickCount++; }
    public String getCode() { return code; }
    public String getDestinationUrl() { return destinationUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public long getClickCount() { return clickCount; }
}
