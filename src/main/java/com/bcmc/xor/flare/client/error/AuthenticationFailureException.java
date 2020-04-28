package com.bcmc.xor.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

@SuppressWarnings("unused")
public class AuthenticationFailureException extends AbstractThrowableProblem {

    public AuthenticationFailureException() {
        super(ErrorConstants.AUTHENTICATION_FAILURE_TYPE, "Incorrect credentials", Status.BAD_REQUEST);
    }
}
