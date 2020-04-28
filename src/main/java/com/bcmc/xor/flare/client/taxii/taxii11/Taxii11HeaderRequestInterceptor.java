package com.bcmc.xor.flare.client.taxii.taxii11;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

import static com.bcmc.xor.flare.client.api.config.Constants.Taxii11Headers.*;

@SuppressWarnings({"unused", "NullableProblems"})
class Taxii11HeaderRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().set(X_TAXII_Accept.getKey(), X_TAXII_Accept.getValue());
        request.getHeaders().set(X_TAXII_Content_Type.getKey(), X_TAXII_Content_Type.getValue());
        request.getHeaders().set(X_TAXII_Protocol.getKey(), X_TAXII_Protocol.getValue());
        request.getHeaders().set(X_TAXII_Services.getKey(), X_TAXII_Services.getValue());
        return execution.execute(request, body);
    }
}
