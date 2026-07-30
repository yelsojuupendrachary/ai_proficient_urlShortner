package ai_pro_url_shortener.url;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import io.swagger.v3.oas.annotations.media.Schema;

public record CreateShortUrlRequest(
        @Schema(description = "Absolute HTTP(S) destination URL", example = "https://example.com/products?id=42")
        @NotBlank @Size(max = 2048) String url,
        @Schema(description = "Optional UTC expiry timestamp; must be future-dated", example = "2026-12-31T23:59:59Z")
        Instant expiresAt) { }
