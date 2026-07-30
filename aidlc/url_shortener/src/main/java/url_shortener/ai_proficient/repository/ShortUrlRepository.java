package url_shortener.ai_proficient.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import url_shortener.ai_proficient.domain.ShortUrl;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    Optional<ShortUrl> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);
}
