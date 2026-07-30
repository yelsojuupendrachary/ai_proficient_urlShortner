package ai_pro_url_shortener.url;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateShortUrlRequest(
        @NotBlank @Size(max = 2048) String url,
        Instant expiresAt) { }
