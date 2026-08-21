package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception;

import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApiErrorCode;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ApplicationException;
import org.springframework.http.HttpStatus;

public class CvPreviewExpiredException extends ApplicationException {
    public CvPreviewExpiredException() {
        super(ApiErrorCode.CV_PREVIEW_EXPIRED, HttpStatus.CONFLICT,
                "Generate a new preview before saving the CV.");
    }
}
