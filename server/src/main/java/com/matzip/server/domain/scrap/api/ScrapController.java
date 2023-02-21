package com.matzip.server.domain.scrap.api;

import com.matzip.server.domain.auth.model.CurrentUser;
import com.matzip.server.domain.auth.model.CurrentUsername;
import com.matzip.server.domain.scrap.dto.ScrapDto.PatchRequest;
import com.matzip.server.domain.scrap.dto.ScrapDto.PostRequest;
import com.matzip.server.domain.scrap.dto.ScrapDto.Response;
import com.matzip.server.domain.scrap.service.ScrapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/scraps")
public class ScrapController {
    private final ScrapService scrapService;

    @PostMapping
    public ResponseEntity<Response> postScrap(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @RequestBody @Valid PostRequest request
    ) {
        log.info("""
                         [{}(id={})] POST /api/v1/scraps
                         \t review ID = {}
                         \t description = {}""", user, myId, request.getReviewId(), request.getDescription());
        return ResponseEntity.ok(scrapService.postScrap(myId, request));
    }

    @PatchMapping("/{review-id}")
    public ResponseEntity<Response> patchScrap(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("review-id") @Positive Long reviewId,
            @RequestBody @Valid PatchRequest request
            ) {
        log.info("[{}(id={})] DELETE /api/v1/scraps/{}", user, myId, reviewId);
        return ResponseEntity.ok(scrapService.patchScrap(myId, reviewId, request));
    }
    @DeleteMapping("/{review-id}")
    public ResponseEntity<Object> deleteScrap(
            @CurrentUser Long myId,
            @CurrentUsername String user,
            @PathVariable("review-id") @Positive Long reviewId
    ) {
        log.info("[{}(id={})] DELETE /api/v1/scraps/{}", user, myId, reviewId);
        scrapService.deleteScrap(myId, reviewId);
        return ResponseEntity.ok().build();
    }
}
