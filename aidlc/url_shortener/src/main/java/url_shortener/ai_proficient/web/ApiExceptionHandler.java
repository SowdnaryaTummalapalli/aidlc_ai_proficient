package url_shortener.ai_proficient.web;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import url_shortener.ai_proficient.service.InvalidUrlException;
import url_shortener.ai_proficient.service.ShortCodeConflictException;
import url_shortener.ai_proficient.service.ShortUrlNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ShortUrlNotFoundException.class) ResponseEntity<Map<String, Object>> missing(RuntimeException ex) { return error(HttpStatus.NOT_FOUND, ex.getMessage()); }
    @ExceptionHandler(ShortCodeConflictException.class) ResponseEntity<Map<String, Object>> conflict(RuntimeException ex) { return error(HttpStatus.CONFLICT, ex.getMessage()); }
    @ExceptionHandler({InvalidUrlException.class, MethodArgumentNotValidException.class}) ResponseEntity<Map<String, Object>> invalid(Exception ex) { return error(HttpStatus.BAD_REQUEST, ex.getMessage()); }
    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) { return ResponseEntity.status(status).body(Map.of("timestamp", Instant.now().toString(), "status", status.value(), "error", status.getReasonPhrase(), "message", message)); }
}
