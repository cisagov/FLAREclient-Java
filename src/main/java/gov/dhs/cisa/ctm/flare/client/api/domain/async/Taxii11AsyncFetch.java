package gov.dhs.cisa.ctm.flare.client.api.domain.async;

import gov.dhs.cisa.ctm.flare.client.api.config.Constants;
import gov.dhs.cisa.ctm.flare.client.api.domain.parameters.Taxii11PollParameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = Constants.RepositoryLabels.ASYNC_FETCH)
public class Taxii11AsyncFetch extends AsyncFetch<Taxii11PollParameters> {

    private static final Logger log = LoggerFactory.getLogger(Taxii11AsyncFetch.class);

    private String apiRootRef;

    public Taxii11AsyncFetch() {
    }

    public Taxii11AsyncFetch(Taxii11PollParameters fetchParams, int window) {
        super(fetchParams, window);
    }

    public String getApiRootRef() {
        return apiRootRef;
    }

    public void setApiRootRef(String apiRootRef) {
        this.apiRootRef = apiRootRef;
    }

    @Override
    public boolean hasNext() {
        return getLastRequested() == null || getLastRequested().plusMillis(this.getWindow()).isBefore(getEnd()) || getLastRequested().plusMillis(this.getWindow()).equals(getEnd());
    }

    @Override
    public FetchChunk next() {
        Instant begin = this.getBegin();
        Instant end;
        if (this.getLastRequested() != null) {
            begin = this.getLastRequested();
        }
        end = begin.plusMillis(getWindow());
        if (end.isAfter(this.getEnd())) {
            end = this.getEnd();
        }
        this.setLastRequested(end);
        log.debug("Next chunk: {} - {}", begin.toString(), end.toString());
        return new FetchChunk<>(begin, end);
    }
}
