package com.bcmc.xor.flare.client.api.domain.audit;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.service.dto.EventDTO;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * An event representing something important that has happened during the application's lifecycle
 */
@Document(collection = Constants.RepositoryLabels.EVENT)
public class Event extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    private EventType type;

    @NotNull
    private String server;

    @Field("taxii_collection")
    private String taxiiCollection;

    private String details;

    @NotNull
    private ZonedDateTime time;

    public Event() {
        this.id = UUID.randomUUID().toString();
    }

    public Event(EventDTO eventDTO) {
        setId(UUID.randomUUID().toString());
        setType(eventDTO.getType());
        setServer(eventDTO.getServer());
        setDetails((eventDTO.getDetails()));
        setTaxiiCollection(eventDTO.getTaxiiCollection());
        setTime(eventDTO.getTime());
    }

    public static Event from(EventType eventType, String details, String server, String collection) {
        Event event = new Event();
        event.setType(eventType);
        event.setDetails(details);
        event.setServer(server);
        event.setTaxiiCollection(collection);
        event.setTime(ZonedDateTime.now());
        return event;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EventType getType() {
        return type;
    }

    private void setType(EventType type) {
        this.type = type;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getTaxiiCollection() {
        return taxiiCollection;
    }

    public void setTaxiiCollection(String taxiiCollection) {
        this.taxiiCollection = taxiiCollection;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public ZonedDateTime getTime() {
        return time;
    }

    private void setTime(ZonedDateTime time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Event{type= " + type + ", server='" + server + "', taxiiCollection='" + taxiiCollection + "', details='" + details + "', time='" + time + "'}";
    }
}
