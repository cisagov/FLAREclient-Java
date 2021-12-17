package com.bcmc.xor.flare.client.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * Log all incoming / outgoing requests that are processed by the
 * Spring Boot filter chain.
 *
 */
public class ReqResUtil {
    private static Logger log = LoggerFactory.getLogger(ReqResUtil.class);
    public static final String PLACEHOLDER_TEXT = "[message-body log disabled]";
    public static final String POSTFIX_TEXT = "...[trucated]";

    private static Gson gson = new Gson();
    private static int HTTP_BODY_LENGTH = 700;

    public static RequestEntity log(RequestEntity request) {
        log.info("Request sent: {}", gson.toJson(requestToJSON(request)));
        return request;
    }

    public static HttpEntity log(HttpEntity request) {
        log.info("Request sent: {}", gson.toJson(requestToJSON(request)));
        return request;
    }

    public static ResponseEntity log(ResponseEntity response) {
        log.info("Response received: {}", gson.toJson(responseToJSON(response)));
        return response;
    }

    /**
     * Get a JSON representation of the request
     *
     * @param request
     *
     * @return
     */
    private static JsonObject requestToJSON(RequestEntity request) {
        JsonObject request_ = new JsonObject();
        if (request == null) {
            return request_;
        }

        request_.addProperty("method", request.getMethod().toString());
        request_.addProperty("request-url", request.getUrl().toString());

        HttpHeaders headers = request.getHeaders();
        if (headers.containsKey("remote_addr"))
            request_.addProperty("client-ip", headers.get("remote_addr").stream().collect(Collectors.joining(",")));

        JsonArray headers_ = new JsonArray();

        for (Map.Entry<String, List<String>> headerEntry : headers.entrySet()) {
            JsonObject header_ = new JsonObject();
            String headerName = headerEntry.getKey();
            List<String> headerValues = headerEntry.getValue();
            header_.addProperty(headerName, headerValues.stream().collect(Collectors.joining(",")));
            headers_.add(header_);
        }

        if (headers_.size() > 0) {
            request_.add("headers", headers_);
        }

        if (request.getBody() != null) {
            if(HTTP_BODY_LENGTH > 0) {
                String body = request.getBody().toString();
                String body_str_cut =
                        body.length() < HTTP_BODY_LENGTH ? body : (body.substring(0, HTTP_BODY_LENGTH) + POSTFIX_TEXT);
                request_.addProperty("message-body", body_str_cut);
            } else {
                request_.addProperty("message-body", PLACEHOLDER_TEXT);
            }
        } else {
            request_.addProperty("message-body", "null");
        }

        JsonObject request_wrapper_ = new JsonObject();
        request_wrapper_.add("request", request_);
        return request_wrapper_;
    }

    /**
     * Note: This duplicates {@link #requestToJSON(RequestEntity)}
     */
    private static JsonObject requestToJSON(HttpEntity request) {
        JsonObject request_ = new JsonObject();
        if (request == null) {
            return request_;
        }

        HttpHeaders headers = request.getHeaders();
        if (headers.containsKey("remote_addr"))
            request_.addProperty("client-ip", headers.get("remote_addr").stream().collect(Collectors.joining(",")));

        JsonArray headers_ = new JsonArray();

        for (Map.Entry<String, List<String>> headerEntry : headers.entrySet()) {
            JsonObject header_ = new JsonObject();
            String headerName = headerEntry.getKey();
            List<String> headerValues = headerEntry.getValue();
            header_.addProperty(headerName, headerValues.stream().collect(Collectors.joining(",")));
            headers_.add(header_);
        }

        if (headers_.size() > 0) {
            request_.add("headers", headers_);
        }

        if (request.getBody() != null) {
            if (HTTP_BODY_LENGTH > 0) {
                String body = request.getBody().toString();
                String body_str_cut =
                        body.length() < HTTP_BODY_LENGTH ? body : (body.substring(0, HTTP_BODY_LENGTH) + POSTFIX_TEXT);
                request_.addProperty("message-body", body_str_cut);
            } else {
                request_.addProperty("message-body", PLACEHOLDER_TEXT);
            }
        } else {
            request_.addProperty("message-body", "null");
        }

        JsonObject request_wrapper_ = new JsonObject();
        request_wrapper_.add("request", request_);
        return request_wrapper_;
    }

    public static JsonObject responseToJSON(ResponseEntity response) {
        JsonObject response_ = new JsonObject();
        response_.addProperty("status-code", response.getStatusCode().toString());

        JsonArray headers_ = new JsonArray();
        HttpHeaders headers = response.getHeaders();

        if (headers.containsKey("content-encoding")) {
            JsonObject contentEncoding = new JsonObject();
            contentEncoding.addProperty("content-encoding", headers.get("content-encoding").stream().collect(Collectors.joining(",")));
            headers_.add(contentEncoding);
        }

        if (headers.getContentType() != null) {
            JsonObject contentType = new JsonObject();
            contentType.addProperty("content-type", headers.getContentType().toString());
            headers_.add(contentType);
        }

        for (Map.Entry<String, List<String>> headerEntry : headers.entrySet()) {
            JsonObject header_ = new JsonObject();
            String headerName = headerEntry.getKey();
            List<String> headerValues = headerEntry.getValue();
            header_.addProperty(headerName, headerValues.stream().collect(Collectors.joining(",")));
            headers_.add(header_);
        }

        if (headers_.size() > 0) {
            response_.add("headers", headers_);
        }


        if (response.getBody() != null) {
            if (HTTP_BODY_LENGTH > 0) {
                String body = response.getBody().toString();
                String body_str_cut =
                        body.length() < HTTP_BODY_LENGTH ? body : (body.substring(0, HTTP_BODY_LENGTH) + POSTFIX_TEXT);
                response_.addProperty("message-body", body_str_cut);
            } else {
                response_.addProperty("message-body", PLACEHOLDER_TEXT);
            }
        } else {
            response_.addProperty("message-body", "null");
        }

        JsonObject response_wrapper_ = new JsonObject();
        response_wrapper_.add("response", response_);
        return response_wrapper_;
    }
}
