package com.bcmc.xor.flare.client.api.domain.server;

import com.bcmc.xor.flare.client.api.domain.collection.TaxiiCollection;
import com.google.gson.annotations.Expose;

import java.net.URI;
import java.util.HashSet;

/**
 * This server indicates a failed creation attempt
 */
public class TemporaryServer extends TaxiiServer {

    @Expose
    private boolean failure;

    @Override
    public HashSet<? extends TaxiiCollection> getCollections() {
        return null;
    }

    @Override
    public URI getServerInformationUrl() {
        return null;
    }

    public boolean isFailure() {
        return failure;
    }

    public void setFailure(boolean failure) {
        this.failure = failure;
    }
}
