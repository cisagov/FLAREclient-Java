package com.bcmc.xor.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class ServerDiscoveryException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;

    public ServerDiscoveryException() {
        super(null, ErrorConstants.SERVER_DISCOVERY_FAILED, Status.BAD_REQUEST);
    }
}
