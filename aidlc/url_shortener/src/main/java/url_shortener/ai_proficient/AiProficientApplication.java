package url_shortener.ai_proficient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import url_shortener.ai_proficient.config.ShortUrlProperties;

@SpringBootApplication
@EnableConfigurationProperties(ShortUrlProperties.class)
public class AiProficientApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiProficientApplication.class, args);
	}

}
