package com.bcmc.xor.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class FlareServiceUnavailableException extends AbstractThrowableProblem {
	private static final long serialVersionUID = 1L;

    public FlareServiceUnavailableException() {
        super(null, ErrorConstants.SERVICE_RETURNED_NO_DATA, Status.SERVICE_UNAVAILABLE);
    }
}
