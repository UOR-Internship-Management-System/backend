package lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.ShortlistController;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.request.ShortlistCandidateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.request.ShortlistCreateRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.request.ShortlistFinalizeRequest;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response.InternshipRequestSummaryResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response.ShortlistCandidateMutationResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response.ShortlistFinalizeResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.api.dto.response.ShortlistResponse;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.application.ShortlistFinalizationService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.application.ShortlistService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.shortlists.domain.policy.ShortlistStatus;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.PreconditionRequiredException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

class ShortlistControllerTest {

    private final ShortlistService shortlistService = mock(ShortlistService.class);
    private final ShortlistFinalizationService finalizationService = mock(ShortlistFinalizationService.class);
    private final ShortlistController controller = new ShortlistController(shortlistService, finalizationService);
    private final UUID shortlistId = UUID.fromString("b3000000-0000-4000-8000-000000000001");

    @Test
    void createReturnsLocationAndStrongVersionEtag() {
        ShortlistCreateRequest request = new ShortlistCreateRequest(UUID.randomUUID(), null, "Finalists");
        when(shortlistService.create(request)).thenReturn(shortlistResponse(0L));

        var response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getFirst(HttpHeaders.LOCATION))
                .endsWith("/api/v1/admin/shortlists/" + shortlistId);
        assertThat(response.getHeaders().getETag()).isEqualTo("\"0\"");
    }

    @Test
    void candidateMutationRequiresQuotedIfMatchAndReturnsNewestEtag() {
        ShortlistCandidateRequest request = new ShortlistCandidateRequest(List.of(UUID.randomUUID()), null);
        when(shortlistService.addCandidates(shortlistId, request, 4L)).thenReturn(
                new ShortlistCandidateMutationResponse(shortlistId, 1, 0, 0, 1, false, 5));

        var response = controller.addCandidates(shortlistId, "\"4\"", request);

        assertThat(response.getHeaders().getETag()).isEqualTo("\"5\"");
        verify(shortlistService).addCandidates(shortlistId, request, 4L);
        assertThatThrownBy(() -> controller.addCandidates(shortlistId, null, request))
                .isInstanceOf(PreconditionRequiredException.class);
    }

    @Test
    void finalizationUsesIfMatchAndReturnsFinalVersionEtag() {
        ShortlistFinalizeRequest request = new ShortlistFinalizeRequest(true, null);
        when(finalizationService.finalizeShortlist(shortlistId, request, 5L)).thenReturn(
                new ShortlistFinalizeResponse(
                        shortlistId,
                        ShortlistStatus.FINALIZED,
                        3,
                        2,
                        true,
                        true,
                        6,
                        OffsetDateTime.parse("2026-08-22T02:30:00Z")));

        var response = controller.finalizeShortlist(shortlistId, "\"5\"", request);

        assertThat(response.getHeaders().getETag()).isEqualTo("\"6\"");
        assertThat(response.getBody().status()).isEqualTo(ShortlistStatus.FINALIZED);
    }

    private ShortlistResponse shortlistResponse(long version) {
        OffsetDateTime now = OffsetDateTime.parse("2026-08-22T02:30:00Z");
        return new ShortlistResponse(
                shortlistId,
                new InternshipRequestSummaryResponse(
                        UUID.randomUUID(), UUID.randomUUID(), "Acme", "Backend Intern", 2),
                null,
                "Finalists",
                ShortlistStatus.DRAFT,
                2,
                0,
                false,
                null,
                version,
                now,
                now,
                null);
    }
}
