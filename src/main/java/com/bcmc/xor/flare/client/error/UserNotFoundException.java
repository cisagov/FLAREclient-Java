package com.bcmc.xor.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class UserNotFoundException extends AbstractThrowableProblem {

    public UserNotFoundException() {
        super(ErrorConstants.USER_NOT_FOUND_TYPE, "User was not found.", Status.BAD_REQUEST);
    }
}
