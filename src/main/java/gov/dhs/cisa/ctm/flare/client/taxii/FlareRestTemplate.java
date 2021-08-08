package gov.dhs.cisa.ctm.flare.client.taxii;

import gov.dhs.cisa.ctm.flare.client.error.ErrorConstants;
import gov.dhs.cisa.ctm.flare.client.error.RequestException;
import gov.dhs.cisa.ctm.flare.client.util.ReqResUtil;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.security.*;
import java.security.cert.CertificateException;

public abstract class FlareRestTemplate extends RestTemplate {

    private static final Logger log = LoggerFactory.getLogger(FlareRestTemplate.class);

    protected FlareRestTemplate(Environment env) {
        // Setup for request factory
        HttpComponentsClientHttpRequestFactory requestFactory = createSSLRequestFactory(
            env.getProperty("server.ssl.key-store"),
            env.getProperty("server.ssl.key-store-password"),
            env.getProperty("server.ssl.trust-store"),
            env.getProperty("server.ssl.trust-store-password"));
        requestFactory.setReadTimeout(60000);               //fixme configurable
        requestFactory.setConnectionRequestTimeout(60000);  //fixme configurable
        requestFactory.setConnectTimeout(60000);            //fixme configurable
        this.setRequestFactory(requestFactory);
    }

    public void setTimeouts(int timeout) {
        HttpComponentsClientHttpRequestFactory requestFactory = (HttpComponentsClientHttpRequestFactory) this.getRequestFactory();
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setConnectionRequestTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        this.setRequestFactory(requestFactory);
    }

    protected <T> ResponseEntity<String> executePost(T request, TaxiiHeaders headers, URI uri) {
        HttpEntity<T> httpEntity = new HttpEntity<>(request, headers);
        ResponseEntity<String> stringResponse = null;
        String feedback = "";
        try {
            ReqResUtil.log(httpEntity);
            stringResponse = this.exchange(uri, HttpMethod.POST, httpEntity, String.class);
            ReqResUtil.log(stringResponse);
        } catch (Exception e) {
            feedback = handleException(e, uri);
        }
        return handleStringResponse(stringResponse, feedback, uri);
    }

    public ResponseEntity<String> executeGet(TaxiiHeaders headers, URI uri) {
        HttpEntity httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> stringResponse = null;
        String feedback = "";
        try {
            ReqResUtil.log(httpEntity);
            stringResponse = this.exchange(uri, HttpMethod.GET, httpEntity, String.class);
            ReqResUtil.log(stringResponse);
        } catch (Exception e) {
            feedback = handleException(e, uri);
        }
        return handleStringResponse(stringResponse, feedback, uri);
    }

    private ResponseEntity<String> handleStringResponse(ResponseEntity<String> stringResponse, String feedback, URI uri) {
        if (stringResponse == null || stringResponse.getBody() == null || stringResponse.getBody().isEmpty()) {
            if (feedback.isEmpty()) {
                feedback = String.format("Response from URI '%s' was empty or null", uri.toString());
            }
            throw new RequestException(feedback, uri.toString(), ErrorConstants.ERR_REQUEST_EXCEPTION);
        }
        return stringResponse;
    }

    private String handleException(Exception e, URI uri) {
        if (e instanceof RestClientResponseException) {
            log.error("Received {} {} from {}. Body: {}",
                ((RestClientResponseException) e).getRawStatusCode(), ((RestClientResponseException) e).getStatusText(),
                uri.toString(), ((RestClientResponseException) e).getResponseBodyAsString());
            return ((RestClientResponseException) e).getResponseBodyAsString();
        } else if (e instanceof RestClientException) {
            log.error("Encountered a client-side error: {}", e.getMessage());
            return e.getCause().getMessage();
        } else {
            log.error("Encountered an exception: {}", e.getMessage());
            return e.getMessage();
        }
    }

    private static HttpComponentsClientHttpRequestFactory createSSLRequestFactory(
        String keystoreFilename, String keystorePass,
        String truststoreFilename, String truststorePass) {

        CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLSocketFactory(createSSLConnectionSocketFactory(
                keystoreFilename, keystorePass,
                truststoreFilename, truststorePass))
            .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        return requestFactory;
    }

    private static SSLConnectionSocketFactory createSSLConnectionSocketFactory (
        String keystoreFilename, String keystorePass,
        String truststoreFilename, String truststorePass) {
        SSLConnectionSocketFactory sslsf;
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            if (keystoreFilename == null || keystorePass == null ||
                truststoreFilename == null | truststorePass == null) {
                log.info("Either keystoreFilename|keystorePass|truststoreFilename|truststorePass is not set.");
                throw new NullPointerException("Either keystoreFilename|keystorePass|truststoreFilename|truststorePass is not set.");
            } else {
                keyStore.load(new FileInputStream((new File(keystoreFilename))), keystorePass.toCharArray());
                sslsf = new SSLConnectionSocketFactory(
                    new SSLContextBuilder()
                        .loadTrustMaterial(new File(truststoreFilename), truststorePass.toCharArray())
                        .loadKeyMaterial(keyStore, keystorePass.toCharArray())
                        .build(), NoopHostnameVerifier.INSTANCE);

            }
        } catch (NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | KeyStoreException | KeyManagementException | IOException e) {
            log.error("Error setting up connection factory: {}", e.getClass(), e);
            throw new RuntimeException(e);
        }
        return sslsf;
    }
}
