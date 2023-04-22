package com.bcmc.xor.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class ManifestNotSupportedException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;

    public ManifestNotSupportedException() {
        super(null, ErrorConstants.MANIFEST_NOT_SUPPORTED, Status.BAD_REQUEST);
    }
}
