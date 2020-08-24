package com.bcmc.xor.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class ServerCredentialsNotFoundException extends AbstractThrowableProblem {

    public ServerCredentialsNotFoundException() {
        super(null, ErrorConstants.CREDENTIALS_NOT_FOUND,  Status.BAD_REQUEST);
    }
}
