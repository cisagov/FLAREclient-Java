package com.bcmc.xor.flare.client.taxii;

import com.bcmc.xor.flare.client.api.domain.server.TaxiiServer;
import com.bcmc.xor.flare.client.api.security.SecurityUtils;
import com.bcmc.xor.flare.client.api.security.ServerCredentialsUtils;
import com.bcmc.xor.flare.client.taxii.taxii11.Taxii11Headers;
import com.bcmc.xor.flare.client.taxii.taxii21.Taxii21Headers;
import org.springframework.http.HttpHeaders;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class TaxiiHeaders extends HttpHeaders {

    protected TaxiiHeaders() {
    }

    /**
     * Set headers with associated 'Authorization' header for a given server
     *
     * Will check the Server Credentials Map for existing credentials
     *
     * @param label the server label to check for credentials
     */
    protected void withAuthorizationForServer(String label) {
        if (SecurityUtils.getCurrentUserLogin().isPresent()) {
            if (ServerCredentialsUtils.getInstance().getServerCredentialsMap().containsKey(SecurityUtils.getCurrentUserLogin().get())) {
                this.withAuthorization(ServerCredentialsUtils.getInstance().getServerCredentialsMap().get(SecurityUtils.getCurrentUserLogin().get()).get(label));
            }
        } else {
            Map<String, String> credentials = ServerCredentialsUtils.getInstance().getServerCredentialsMap().values()
                .stream().filter(map -> map.keySet().contains(label)).findAny()
                .orElseThrow(() -> new IllegalStateException("No credentials available to provide authorization to server " + label));
            this.withAuthorization(credentials.get(label));
        }
    }

    private void withAuthorization(String authHeader) {
        this.add("Authorization", authHeader);
    }

    public TaxiiHeaders withHeader(String key, List<String> value) {
        this.put(key, value);
        return this;
    }

    public TaxiiHeaders withHeader(String key, String value) {
        this.put(key, Collections.singletonList(value));
        return this;
    }


    /**
     * Generate TaxiiHeaders from a given Server
     *
     * Checks the TAXII Version of the server, and also if the server will require a HTTP Basic Auth header. If it does, this method will generate that header and return a
     * TaxiiHeaders object
     * @param server the server that will be checked for version and Basic auth requirements
     * @return TaxiiHeaders containing the appropriate headers for the given server version and auth requirements
     */
    public static TaxiiHeaders fromServer(TaxiiServer server) {
        switch (server.getVersion()) {
            case TAXII21:
                Taxii21Headers taxii21Headers = new Taxii21Headers();
                if (server.getRequiresBasicAuth()) {
                    taxii21Headers.withAuthorizationForServer(server.getLabel());
                }
                return taxii21Headers;
            case TAXII11:
                Taxii11Headers taxii11Headers = new Taxii11Headers();
                if (server.getRequiresBasicAuth()) {
                    taxii11Headers.withAuthorizationForServer(server.getLabel());
                }
                return taxii11Headers;
            default:
                return new TaxiiHeaders();
        }
    }

}
