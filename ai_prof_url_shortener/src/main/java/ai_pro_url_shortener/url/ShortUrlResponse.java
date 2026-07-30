package ai_pro_url_shortener.url;

import java.time.Instant;

public record ShortUrlResponse(String code, String shortUrl, String destinationUrl,
                               Instant createdAt, Instant expiresAt) { }
