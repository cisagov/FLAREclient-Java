package com.bcmc.xor.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class UserNotFoundException extends AbstractThrowableProblem {

	private static final long serialVersionUID = 1L;

	public UserNotFoundException() {
        super(null, ErrorConstants.USER_NOT_FOUND,  Status.NOT_FOUND);
    }
}
