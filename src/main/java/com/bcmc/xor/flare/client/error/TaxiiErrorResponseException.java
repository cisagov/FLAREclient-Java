package com.bcmc.xor.flare.client.error;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;
import xor.bcmc.taxii2.messages.TaxiiError;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TaxiiErrorResponseException extends AbstractThrowableProblem {
    private static final Gson gson = new Gson();

	private static final long serialVersionUID = 1L;
	private final TaxiiError taxiiError;
    private final String message;

    public TaxiiErrorResponseException(TaxiiError taxiiError) {
        super(ErrorConstants.DEFAULT_TYPE, taxiiError.getDescription(), Status.INTERNAL_SERVER_ERROR, taxiiError.getErrorCode(), null, null, getAlertParameters(taxiiError));
        this.taxiiError = taxiiError;
        this.message = String.format("%s - %s", taxiiError.getErrorCode(), taxiiError.getDescription());
    }

    @Override
    public String getMessage() {
        return message;
    }

    public TaxiiError getTaxiiError() {
        return taxiiError;
    }

    private static Map<String, Object> getAlertParameters(TaxiiError taxiiError) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("message", taxiiError.getDescription());
        parameters.put("params", "status");
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> detailsMap = gson.fromJson(taxiiError.getDetails(), type);
        parameters.putAll(detailsMap);
        return parameters;
    }

}
