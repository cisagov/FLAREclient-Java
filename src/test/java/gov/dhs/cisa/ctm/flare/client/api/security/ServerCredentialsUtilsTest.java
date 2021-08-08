package gov.dhs.cisa.ctm.flare.client.api.security;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.dhs.cisa.ctm.flare.client.api.security.ServerCredentialsUtils;

import static org.junit.Assert.assertEquals;

public class ServerCredentialsUtilsTest {

    private static final Logger log = LoggerFactory.getLogger(ServerCredentialsUtilsTest.class);

    @Test
    public void generateBasicAuthHeaderTest() {
        //Verified against: https://www.blitter.se/utils/basic-authentication-header-generator/

        String basicAuthHeader = ServerCredentialsUtils.generateBasicAuthHeader("client1", "pword");
        assertEquals("Basic Y2xpZW50MTpwd29yZA==", basicAuthHeader);
//        log.info(basicAuthHeader);

        basicAuthHeader = ServerCredentialsUtils.generateBasicAuthHeader("user", "password");
        assertEquals("Basic dXNlcjpwYXNzd29yZA==", basicAuthHeader);
    }
}
