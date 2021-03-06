package com.bcmc.xor.flare.client.taxii.taxii21;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.server.Taxii21Server;
import com.bcmc.xor.flare.client.api.domain.server.TaxiiServer;
import com.bcmc.xor.flare.client.api.domain.status.Status;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.api.service.dto.ServerDTO;
import com.bcmc.xor.flare.client.error.TaxiiErrorResponseException;
import com.bcmc.xor.flare.client.taxii.FlareRestTemplate;
import com.bcmc.xor.flare.client.taxii.TaxiiHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import xor.bcmc.taxii2.JsonHandler;
import xor.bcmc.taxii2.messages.TaxiiError;
import xor.bcmc.taxii2.resources.ApiRoot;
import xor.bcmc.taxii2.resources.Collections;
import xor.bcmc.taxii2.resources.Discovery;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Taxii21RestTemplate extends FlareRestTemplate {

    private static final Logger log = LoggerFactory.getLogger(ServerService.class);

    public Taxii21RestTemplate(Environment env) {
        super(env);
        // Setup for message conversion
        HttpMessageConverter taxii21gson = new GsonHttpMessageConverter(xor.bcmc.taxii2.JsonHandler.getInstance().getGson());
        List<HttpMessageConverter<?>> taxii21converters = new ArrayList<>();
        taxii21converters.add(new StringHttpMessageConverter());
        taxii21converters.add(taxii21gson);
        this.setMessageConverters(taxii21converters);

    }

    public Discovery discovery(TaxiiServer server) {
        ResponseEntity<String> responseEntity = executeGet(TaxiiHeaders.fromServer(server), server.getServerInformationUrl());
        if (responseEntity.getStatusCode().isError()) {
            TaxiiError taxiiError = JsonHandler.fromJson(responseEntity.getBody(), TaxiiError.class);
            throw new TaxiiErrorResponseException(taxiiError);
        } else {
            return JsonHandler.fromJson(responseEntity.getBody(), Discovery.class);
        }
    }

    public Discovery discovery(ServerDTO serverDTO) {
        this.setTimeouts(20000);
        Discovery response = discovery(new Taxii21Server(serverDTO));
        this.setTimeouts(60000);
        return response;
    }

    public ApiRoot getApiRoot(Taxii21Server server, URI uri) {
        log.info("Attempting to get ApiRoot information from '{}'", uri.toString());
        ResponseEntity<String> responseEntity = executeGet(TaxiiHeaders.fromServer(server), uri);
        if (responseEntity.getStatusCode().isError()) {
            TaxiiError taxiiError = JsonHandler.fromJson(responseEntity.getBody(), TaxiiError.class);
            throw new TaxiiErrorResponseException(taxiiError);
        } else {
            return JsonHandler.fromJson(responseEntity.getBody(), ApiRoot.class);
        }
    }

    public Collections getCollections(Taxii21Server server, URI uri) {
        log.info("Attempting to get Collection information from '{}'", uri.toString());
        ResponseEntity<String> responseEntity = executeGet(TaxiiHeaders.fromServer(server), uri);
        if (responseEntity.getStatusCode().isError()) {
            TaxiiError taxiiError = JsonHandler.fromJson(responseEntity.getBody(), TaxiiError.class);
            throw new TaxiiErrorResponseException(taxiiError);
        } else {
            return JsonHandler.fromJson(responseEntity.getBody(), Collections.class);
        }
    }

    public Status getStatus(Taxii21Server server, URI uri) {
        log.info("Attempting to get Status from '{}'", uri.toString());
        ResponseEntity<String> responseEntity = executeGet(TaxiiHeaders.fromServer(server), uri);
        if (responseEntity.getStatusCode().isError()) {
            TaxiiError taxiiError = JsonHandler.fromJson(responseEntity.getBody(), TaxiiError.class);
            throw new TaxiiErrorResponseException(taxiiError);
        } else {
            return JsonHandler.getInstance().getGson().fromJson(responseEntity.getBody(), Status.class);
        }
    }

    public Status postEnvelope(Taxii21Server server, URI uri, String bundle) {
        log.info("Attempting to POST content to '{}' with Content-Type: {}", uri.toString(), Constants.HEADER_TAXII21_JSON_VERSION_21);
        TaxiiHeaders headers = Taxii21Headers.fromServer(server).withHeader("Content-Type", Constants.HEADER_TAXII21_JSON_VERSION_21);
         ResponseEntity<String> responseEntity = executePost(bundle, headers, uri);

        if (responseEntity.getStatusCode().isError()) {
            TaxiiError taxiiError = JsonHandler.fromJson(responseEntity.getBody(), TaxiiError.class);
            throw new TaxiiErrorResponseException(taxiiError);
        } else {
            return JsonHandler.getInstance().getGson().fromJson(responseEntity.getBody(), Status.class);
        }
    }

    public String getManifest(Taxii21Server server, URI uri) {
        log.info("Attempting to GET manifest to '{}'", uri.toString());
        ResponseEntity<String> responseEntity = executeGet(TaxiiHeaders.fromServer(server).withHeader("Accept", Arrays.asList(Constants.HEADER_STIX21_JSON, Constants.HEADER_TAXII21_JSON)), uri);
        if (responseEntity.getStatusCode().isError()) {
            TaxiiError taxiiError = JsonHandler.fromJson(responseEntity.getBody(), TaxiiError.class);
            throw new TaxiiErrorResponseException(taxiiError);
        } else {
            return responseEntity.getBody();
        }
    }

}
