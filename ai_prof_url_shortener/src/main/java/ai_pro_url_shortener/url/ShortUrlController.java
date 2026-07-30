package ai_pro_url_shortener.url;

import jakarta.validation.Valid;
import java.net.URI;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Short URLs", description = "Create and resolve short links, then inspect aggregate usage.")
public class ShortUrlController {
    private final ShortUrlService service;
    public ShortUrlController(ShortUrlService service) { this.service = service; }

    @PostMapping("/api/urls")
    @Operation(summary = "Create a short URL", description = "Accepts an absolute HTTP(S) destination and optional future expiry time.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Short URL created"),
            @ApiResponse(responseCode = "400", description = "Invalid URL or expiry")
    })
    public ResponseEntity<ShortUrlResponse> create(@Valid @RequestBody CreateShortUrlRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{code:[a-zA-Z0-9]{8}}")
    @Operation(summary = "Resolve a short URL", description = "Redirects to the destination and increments aggregate click count.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirect to destination"),
            @ApiResponse(responseCode = "404", description = "Code does not exist"),
            @ApiResponse(responseCode = "410", description = "Link has expired")
    })
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(service.resolveAndRegisterClick(code))).build();
    }

    @GetMapping("/api/urls/{code}/analytics")
    @Operation(summary = "Retrieve short URL analytics", description = "Returns aggregate click count and link metadata; no personal tracking data is collected.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Analytics returned"),
            @ApiResponse(responseCode = "404", description = "Code does not exist")
    })
    public AnalyticsResponse analytics(@PathVariable String code) { return service.analytics(code); }
}
