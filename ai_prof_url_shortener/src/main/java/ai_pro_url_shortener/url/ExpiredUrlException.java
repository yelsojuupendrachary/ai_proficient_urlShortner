package ai_pro_url_shortener.url;

public class ExpiredUrlException extends RuntimeException {
    public ExpiredUrlException(String code) { super("The short URL for code '" + code + "' has expired."); }
}
