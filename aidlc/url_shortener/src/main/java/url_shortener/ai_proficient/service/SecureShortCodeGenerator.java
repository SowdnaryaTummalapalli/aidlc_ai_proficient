package url_shortener.ai_proficient.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;
import url_shortener.ai_proficient.config.ShortUrlProperties;

@Component
public class SecureShortCodeGenerator implements ShortCodeGenerator {
    private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private final SecureRandom random = new SecureRandom();
    private final int length;
    public SecureShortCodeGenerator(ShortUrlProperties properties) { this.length = properties.codeLength(); }
    @Override public String nextCode() {
        StringBuilder result = new StringBuilder(length);
        for (int index = 0; index < length; index++) result.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        return result.toString();
    }
}
