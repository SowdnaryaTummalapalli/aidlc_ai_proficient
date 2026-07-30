package url_shortener.ai_proficient.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import url_shortener.ai_proficient.domain.ClickEvent;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {
    long countByShortUrl_Id(Long shortUrlId);
    @Query("select c from ClickEvent c where c.shortUrl.id = :shortUrlId and c.clickedAt >= :from and c.clickedAt < :until order by c.clickedAt")
    List<ClickEvent> findAllInRange(@Param("shortUrlId") Long shortUrlId, @Param("from") Instant from, @Param("until") Instant until);
}
