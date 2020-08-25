package com.bcmc.xor.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class CollectionNotFoundException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;

    public CollectionNotFoundException() {
        super(null, ErrorConstants.COLLECTION_NOT_FOUND, Status.BAD_REQUEST);
    }
}
