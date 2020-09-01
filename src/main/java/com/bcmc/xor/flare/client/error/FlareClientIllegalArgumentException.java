package com.bcmc.xor.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.util.Map;

public class FlareClientIllegalArgumentException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;
    
    public FlareClientIllegalArgumentException(Map<String, Object> badParameterMap) {
        super(null, String.valueOf(badParameterMap),  Status.BAD_REQUEST);
    }
}
