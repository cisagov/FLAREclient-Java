package com.bcmc.xor.flare.client.api.domain.async;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.content.CountResult;
import com.bcmc.xor.flare.client.api.domain.parameters.ApiParameters;
import com.bcmc.xor.flare.client.api.domain.parameters.Taxii11PollParameters;
import com.bcmc.xor.flare.client.api.domain.parameters.Taxii20GetParameters;
import com.bcmc.xor.flare.client.taxii.TaxiiAssociation;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Iterator;
import java.util.Objects;

@Document(collection = Constants.RepositoryLabels.ASYNC_FETCH)
public abstract class AsyncFetch<T extends ApiParameters> implements Iterator<FetchChunk> {

    @Id
    private String id;

    private Status status;
    private TaxiiAssociation association;
    private Instant requestedAt;
    private Instant begin;
    private Instant lastRequested;
    private Instant end;

    /**
     * Window in milliseconds
     */
    private int window;
    private T initialFetchParams;
    private CountResult countResult;

    public AsyncFetch() {
    }

    public AsyncFetch(TaxiiAssociation association, Instant requestedAt, Instant begin, Instant end, int window, T initialFetchParams) {
        this.association = association;
        this.status = Status.PENDING;
        this.requestedAt = requestedAt;
        this.begin = begin;
        this.end = end;
        this.window = window;
        this.initialFetchParams = initialFetchParams;
    }

    AsyncFetch(T fetchParams, int window) {
        this.association = fetchParams.getAssociation();
        this.status = Status.PENDING;
        this.window = window;
        this.requestedAt = Instant.now();
        if (fetchParams instanceof Taxii20GetParameters) {
            this.begin = ((Taxii20GetParameters) fetchParams).getAddedAfter().toInstant();
            this.end = null;
        } else if (fetchParams instanceof Taxii11PollParameters) {
            this.begin = ((Taxii11PollParameters) fetchParams).getStartDate().toInstant();
            this.end = ((Taxii11PollParameters) fetchParams).getEndDate().toInstant();
        }
        this.initialFetchParams = fetchParams;
    }

    public enum Status {
        COMPLETE,
        PENDING,
        FETCHING,
        ERROR
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    void setRequestedAt(Instant requestedAt) {
        this.requestedAt = requestedAt;
    }

    public Instant getBegin() {
        return begin;
    }

    void setBegin(Instant begin) {
        this.begin = begin;
    }

    public Instant getEnd() {
        return end;
    }

    void setEnd(Instant end) {
        this.end = end;
    }

    int getWindow() {
        return window;
    }

    void setWindow(int window) {
        this.window = window;
    }

    public AsyncFetch withWindow(int window) {
        this.setWindow(window);
        return this;
    }

    public TaxiiAssociation getAssociation() {
        return association;
    }

    void setAssociation(TaxiiAssociation association) {
        this.association = association;
    }

    public T getInitialFetchParams() {
        return initialFetchParams;
    }

    public void setInitialFetchParams(T initialFetchParams) {
        this.initialFetchParams = initialFetchParams;
    }

    public CountResult getCountResult() {
        return countResult;
    }

    public void setCountResult(CountResult countResult) {
        this.countResult = countResult;
    }

    public Instant getLastRequested() {
        return lastRequested;
    }

    public void setLastRequested(Instant lastRequested) {
        this.lastRequested = lastRequested;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AsyncFetch that = (AsyncFetch) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
