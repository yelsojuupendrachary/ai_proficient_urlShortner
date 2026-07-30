package ai_pro_url_shortener.url;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShortUrlService {
    private static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int CODE_LENGTH = 8;
    private final ShortUrlRepository repository;
    private final SecureRandom random = new SecureRandom();
    private final Clock clock;
    private final String baseUrl;

    @Autowired
    public ShortUrlService(ShortUrlRepository repository, @Value("${app.base-url}") String baseUrl) {
        this(repository, baseUrl, Clock.systemUTC());
    }

    ShortUrlService(ShortUrlRepository repository, String baseUrl, Clock clock) {
        this.repository = repository;
        this.baseUrl = baseUrl.replaceAll("/$", "");
        this.clock = clock;
    }

    @Transactional
    public ShortUrlResponse create(CreateShortUrlRequest request) {
        validateDestination(request.url());
        Instant now = clock.instant();
        if (request.expiresAt() != null && !request.expiresAt().isAfter(now)) {
            throw new IllegalArgumentException("expiresAt must be in the future.");
        }
        for (int attempt = 0; attempt < 10; attempt++) {
            String code = nextCode();
            if (!repository.existsById(code)) {
                ShortUrl saved = repository.save(new ShortUrl(code, request.url(), now, request.expiresAt()));
                return toResponse(saved);
            }
        }
        throw new IllegalStateException("Could not allocate a unique short code; retry the request.");
    }

    @Transactional
    public String resolveAndRegisterClick(String code) {
        ShortUrl shortUrl = find(code);
        if (shortUrl.getExpiresAt() != null && !shortUrl.getExpiresAt().isAfter(clock.instant())) {
            throw new ExpiredUrlException(code);
        }
        shortUrl.registerClick();
        return shortUrl.getDestinationUrl();
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse analytics(String code) {
        ShortUrl shortUrl = find(code);
        return new AnalyticsResponse(shortUrl.getCode(), shortUrl.getDestinationUrl(), shortUrl.getClickCount(), shortUrl.getCreatedAt(), shortUrl.getExpiresAt());
    }

    private ShortUrl find(String code) { return repository.findById(code).orElseThrow(() -> new UrlNotFoundException(code)); }
    private ShortUrlResponse toResponse(ShortUrl url) { return new ShortUrlResponse(url.getCode(), baseUrl + "/" + url.getCode(), url.getDestinationUrl(), url.getCreatedAt(), url.getExpiresAt()); }
    private String nextCode() { StringBuilder code = new StringBuilder(CODE_LENGTH); for (int i = 0; i < CODE_LENGTH; i++) code.append(ALPHABET[random.nextInt(ALPHABET.length)]); return code.toString(); }

    private void validateDestination(String value) {
        try {
            URI uri = new URI(value);
            if (!uri.isAbsolute() || uri.getHost() == null || !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) {
                throw new IllegalArgumentException("url must be an absolute HTTP or HTTPS URL.");
            }
        } catch (URISyntaxException ex) { throw new IllegalArgumentException("url must be a valid URI."); }
    }
}
