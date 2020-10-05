package com.bcmc.xor.flare.client.error;

import java.net.URI;

public final class ErrorConstants {

    public static final String ERR_REQUEST_EXCEPTION = "error.requestException";
    public static final String ERR_CONCURRENCY_FAILURE = "error.concurrencyFailure";
    public static final String ERR_VALIDATION = "error.validation";
    public static final String ERR_BAD_REQUEST = "error.request";
    public static final String ERR_EMAIL_IN_USED = "error.email.in.used";
    public static final String ERR_LOGIN_IN_USED = "error.login.in.used";
//    public static final String ERR_ACTIVATIONKEY_NOT_FOUND = "error.activation.key.not.found";
//    public static final String ERR_ACCOUNT_UPDATE = "error.accout.update";
//    public static final String ERR_USER_NOT_FOUND = "error.accout.user.not.found";
    
    private static final String PROBLEM_BASE_URL = "";
    public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "/problem-with-message");
    public static final URI RUNTIME_TYPE = URI.create(PROBLEM_BASE_URL + "/runtime");
    public static final URI INTERNAL_TYPE = URI.create(PROBLEM_BASE_URL + "/internal");
    public static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(PROBLEM_BASE_URL + "/constraint-violation");
    public static final URI PARAMETERIZED_TYPE = URI.create(PROBLEM_BASE_URL + "/parameterized");
    public static final URI ENTITY_NOT_FOUND_TYPE = URI.create(PROBLEM_BASE_URL + "/entity-not-found");
    public static final URI INVALID_PASSWORD_TYPE = URI.create(PROBLEM_BASE_URL + "/invalid-password");
    public static final URI AUTHENTICATION_FAILURE_TYPE = URI.create(PROBLEM_BASE_URL + "/invalid-credentials");
    public static final URI EMAIL_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "/email-already-used");
    public static final URI DATE_FORMAT_ERROR_TYPE = URI.create(PROBLEM_BASE_URL + "/bad-date");
    public static final URI LOGIN_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "/login-already-used");
    public static final URI EMAIL_NOT_FOUND_TYPE = URI.create(PROBLEM_BASE_URL + "/email-not-found");
//    public static final URI USER_NOT_FOUND_TYPE = URI.create(PROBLEM_BASE_URL + "/user-not-found");
    public static final URI ACTIVATION_ERROR = URI.create(PROBLEM_BASE_URL + "/activation-error");
    public static final URI ACCOUNT_UPDATE_ERROR = URI.create(PROBLEM_BASE_URL + "/account-update-error");
    public static final URI AUDIT_EVENT_NOT_FOUND = URI.create(PROBLEM_BASE_URL + "/audit-event-no-found-error");

    public static final String INVALID_PASSWORD = "Invalid Password.";
    public static final String EMAIL_ALREADY_USED = "Email is already in use.";
    public static final String LOGIN_ALREADY_USED = "Login already exists.";
    public static final String USER_NOT_FOUND = "User was not found.";
    public static final String SERVER_NOT_FOUND = "Server was not found.";
    public static final String SERVER_DISCOVERY_FAILED = "Server discovery information was not returned. Check server discovery URL.";
    public static final String SERVER_NOT_CREATED = "Server was not created.  Check server details.";
    public static final String SERVER_LABEL_ALREADY_USED = "Server label already exists.";
    public static final String CREDENTIALS_NOT_FOUND = "No server credentials exist for the user.";
    public static final String USERNAME_REQUIRED_PARAM = "Username is a required parameter.";
    public static final String PASSWORD_REQUIRED_PARAM = "Password is a required parameter.";
    public static final String AUTHENTICATION_FAILURE = "Authentication failed.";
    public static final String COLLECTION_NOT_FOUND = "Collection was not found.";
    public static final String ILLEGAL_ARG_USER_ID = "Request body to create a new user cannot contain an 'id' field.";
    public static final String ILLEGAL_ARG_MISSING_USER_ID = "Request body to update a new user must contain an 'id' field.";
    public static final String SERVICE_RETURNED_NO_DATA = "No data was returned by service.  Check server logs.";
    public static final String NAME_IS_NULL = "Request body 'name' field cannot be null.";

    private ErrorConstants() {
    }
}
