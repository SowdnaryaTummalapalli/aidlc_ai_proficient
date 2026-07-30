package url_shortener.ai_proficient.service;
public class ShortUrlNotFoundException extends RuntimeException { public ShortUrlNotFoundException(String code) { super("Short URL not found: " + code); } }
