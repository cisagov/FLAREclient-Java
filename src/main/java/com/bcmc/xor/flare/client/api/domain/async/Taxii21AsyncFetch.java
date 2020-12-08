package com.bcmc.xor.flare.client.api.domain.async;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.parameters.Taxii21GetParameters;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = Constants.RepositoryLabels.ASYNC_FETCH)
public class Taxii21AsyncFetch extends AsyncFetch<Taxii21GetParameters> {

    private String apiRootRef;

    public Taxii21AsyncFetch() {
    }

    public Taxii21AsyncFetch(Taxii21GetParameters fetchParams) {
        super(fetchParams, 0);
    }

    protected FetchChunk calculateNextChunk() {
        return null;
    }

    public String getApiRootRef() {
        return apiRootRef;
    }

    public void setApiRootRef(String apiRootRef) {
        this.apiRootRef = apiRootRef;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public FetchChunk next() {
        return null;
    }
}
