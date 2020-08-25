package com.bcmc.xor.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.util.ArrayList;
import java.util.Map;

public class FlareClientIllegalArgumentException extends AbstractThrowableProblem {

    public FlareClientIllegalArgumentException(Map<String, Object> badParameterMap) {
        super(null, String.valueOf(badParameterMap),  Status.BAD_REQUEST);
    }
}
