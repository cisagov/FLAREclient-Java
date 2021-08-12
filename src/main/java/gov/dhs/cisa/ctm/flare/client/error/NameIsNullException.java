package gov.dhs.cisa.ctm.flare.client.error;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

public class NameIsNullException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;

    public NameIsNullException() {
        super(null,ErrorConstants.NAME_IS_NULL, Status.BAD_REQUEST);
    }
}
