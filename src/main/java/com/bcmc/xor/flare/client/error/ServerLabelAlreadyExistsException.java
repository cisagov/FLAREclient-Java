package com.bcmc.xor.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class ServerLabelAlreadyExistsException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;

    public ServerLabelAlreadyExistsException() {
        super(null, ErrorConstants.SERVER_LABEL_ALREADY_USED, Status.BAD_REQUEST);
    }
}
