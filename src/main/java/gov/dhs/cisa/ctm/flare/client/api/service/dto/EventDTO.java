package gov.dhs.cisa.ctm.flare.client.api.service.dto;

import java.time.ZonedDateTime;

import gov.dhs.cisa.ctm.flare.client.api.domain.audit.Event;
import gov.dhs.cisa.ctm.flare.client.api.domain.audit.EventType;

/**
 *  * A DTO representing a server
 */

public class EventDTO {

    private static final long serialVersionUID = 1L;

    private final String id;

    private final EventType type;

    private final String server;

    private final String taxiiCollection;

    private final String details;

    private final ZonedDateTime time;

    public EventDTO(Event event) {
        this.id = event.getId();
        this.type = event.getType();
        this.server = event.getServer();
        this.taxiiCollection = event.getTaxiiCollection();
        this.details = event.getDetails();
        this.time = event.getTime();
    }

    public String getId() {
        return id;
    }

    public EventType getType() {
        return type;
    }

    public String getServer() {
        return server;
    }

    public String getTaxiiCollection() {
        return taxiiCollection;
    }

    public String getDetails() {
        return details;
    }

    public ZonedDateTime getTime() {
        return time;
    }

}
