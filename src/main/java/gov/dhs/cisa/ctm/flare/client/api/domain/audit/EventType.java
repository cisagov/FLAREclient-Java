package gov.dhs.cisa.ctm.flare.client.api.domain.audit;

/**
 * The EventType enumeration.
 */
public enum EventType {

    // Content Download
    FETCHED_CONTENT,
    FETCH_CONTENT_FAILED,
    @SuppressWarnings("unused") CONTENT_VALIDATED,
    RECURRING_POLL_STARTED,
    ASYNC_FETCH_STARTED,
    ASYNC_FETCH_UPDATE,
    ASYNC_FETCH_COMPLETE,
    ASYNC_FETCH_ERROR,
    RECURRING_POLL_DELETED,
    VALIDATION,

    // Content Upload
    PUBLISHED,
    PUBLISH_FAILED,
    STATUS_CREATED,
    STATUS_UPDATED,
    STATUS_UPDATE_FAILURE,
    OUTBOX_STARTED,
    OUTBOX_PUBLISHED,
    OUTBOX_PUBLISH_FAILED,
    OUTBOX_STOPPED,

    // Servers
    SERVER_ADDED,
    SERVER_CONFIGURED,
    SERVER_UPDATED,
    SERVER_UNAVAILABLE,
    COLLECTION_INFO_REQUEST_FAILED,
    @SuppressWarnings("unused") SUBSCRIPTION_INFO_REQUEST_FAILED,
    SERVER_DELETED,

    // Collections
    COLLECTION_ADDED,
    COLLECTION_REFRESHED,
    @SuppressWarnings("unused") COLLECTION_UNAVAILABLE,
    EXPORTED_CONTENT,

    // General
    ERROR
}
