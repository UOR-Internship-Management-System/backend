package lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application;

import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.api.dto.response.CvResponse;

/** HTTP-neutral result carrying whether the active resource was created or replaced. */
public record CvSaveResult(CvResponse response, boolean created) {}
