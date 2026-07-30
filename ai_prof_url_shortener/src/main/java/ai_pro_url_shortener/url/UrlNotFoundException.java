package ai_pro_url_shortener.url;

public class UrlNotFoundException extends RuntimeException {
    public UrlNotFoundException(String code) { super("No short URL exists for code '" + code + "'."); }
}
