package ai_pro_url_shortener.url;

import java.time.Instant;

public record AnalyticsResponse(String code, String destinationUrl, long clickCount,
                                Instant createdAt, Instant expiresAt) { }
