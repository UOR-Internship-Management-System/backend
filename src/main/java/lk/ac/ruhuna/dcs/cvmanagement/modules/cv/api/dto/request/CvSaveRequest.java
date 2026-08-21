package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CvSaveRequest(@NotNull UUID previewId) {
}
