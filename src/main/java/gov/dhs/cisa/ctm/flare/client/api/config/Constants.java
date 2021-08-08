package gov.dhs.cisa.ctm.flare.client.api.config;

import com.google.common.collect.Sets;
import org.mitre.taxii.messages.xml11.ContentBindingIDType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Application constants.
 */
@SuppressWarnings("unused")
public final class Constants {

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^[_.@A-Za-z0-9-]*$";
    public static final String UUID_REGEX = "^[0-9A-F]{8}-[0-9A-F]{4}-4[0-9A-F]{3}-[89AB][0-9A-F]{3}-[0-9A-F]{12}$";

    public static final String SYSTEM_ACCOUNT = "system";
    public static final String ANONYMOUS_USER = "anonymoususer";
    public static final String DEFAULT_LANGUAGE = "en";

    public static final String HEADER_TAXII11_MESSAGE_BINDING = "urn:taxii.mitre.org:message:xml:1.1";
    public static final String HEADER_TAXII11_ACCEPT = "urn:taxii.mitre.org:message:xml:1.1";
    public static final String HEADER_TAXII11_SERVICES = "urn:taxii.mitre.org:services:1.1";
    public static final String HEADER_TAXII11_PROTOCOL = "urn:taxii.mitre.org:protocol:https:1.0";

    public static final String HEADER_TAXII21_JSON = "application/taxii+json;version=2.1";
    public static final String HEADER_TAXII21_JSON_VERSION_21 = "application/taxii+json;version=2.1";
    public static final String HEADER_STIX21_JSON = "application/stix+json";
    public static final String HEADER_STIX21_JSON_VERSION_21 = "application/stix+json;version=2.1";

    public static final Set<String> STIX20_TYPES = Sets.newHashSet(
        "attack-pattern",
        "campaign",
        "course-of-action",
        "identity",
        "indicator",
        "intrusion-set",
        "malware",
        "observed-data",
        "report",
        "threat-actor",
        "tool",
        "vulnerability",
        "relationship",
        "sighting"
    );

    public static final Map<String, String> stix1ContentBindingsStringMap;
    public static final Map<String, ContentBindingIDType> stix1ContentBindingsMap;

    static {

        Map<String, String> tempMapForStrings = new HashMap<>(5, 1.0F);
        tempMapForStrings.put("1.2", "urn:stix.mitre.org:xml:1.2");
        tempMapForStrings.put("1.1.1", "urn:stix.mitre.org:xml:1.1.1");
        tempMapForStrings.put("1.1", "urn:stix.mitre.org:xml:1.1");
        tempMapForStrings.put("1.0.1", "urn:stix.mitre.org:xml:1.0.1");
        tempMapForStrings.put("1.0", "urn:stix.mitre.org:xml:1.0");
        stix1ContentBindingsStringMap = Collections.unmodifiableMap(tempMapForStrings);

        Map<String, ContentBindingIDType> tempMap = new HashMap<>(5, 1.0F);
        for (String value: stix1ContentBindingsStringMap.values()) {
            tempMap.put(value, new ContentBindingIDType(null, value));
        }

        stix1ContentBindingsMap = Collections.unmodifiableMap(tempMap);
    }

    // TAXII
    public static final String TAXII2_TAXII_ENDPOINT = "/taxii2/";
    public static final String TAXII2_STATUS_ENDPOINT = "status/";
    public static final String TAXII2_COLLECTIONS_ENDPOINT = "collections/";
    public static final String TAXII2_OBJECTS_ENDPOINT = "objects/";

    private Constants() {
    }

    public static class RepositoryLabels {
        public static final String API_ROOT = "api_root";
        public static final String ASYNC_FETCH = "async_fetch";
        public static final String ASYNC_FETCH_CHUNK = "async_fetch_chunk";
        public static final String AUTHORITY = "authority";
        public static final String TAXII_COLLECTION = "taxii_collection";
        public static final String CONTENT = "content";
        public static final String EVENT = "event";
        public static final String PERSISTENCE_AUDIT_EVENT = "audit";
        public static final String RECURRING_FETCH = "recurring_fetch";
        public static final String SERVER = "server";
        public static final String STATUS = "status";
        public static final String USER = "user";
    }

    public enum TaxiiVersion {
        TAXII11, TAXII21, UNKNOWN
    }

    public enum StixVersion {
        STIX10, STIX101, STIX11, STIX111, STIX12, STIX20
    }

    public enum StatusType {
        PENDING, COMPLETE
    }

    public static class ContentBindings {
        public static final String STIX10 = "urn:stix.mitre.org:xml:1.0";
        public static final String STIX101 = "urn:stix.mitre.org:xml:1.0.1";
        public static final String STIX11 = "urn:stix.mitre.org:xml:1.1";
        public static final String STIX111 = "urn:stix.mitre.org:xml:1.1.1";
        public static final String STIX12 = "urn:stix.mitre.org:xml:1.2";
        public static final HashMap<String, String> contentBindingMap;

        static {
            contentBindingMap = new HashMap<>();
            contentBindingMap.put("1.0", STIX10);
            contentBindingMap.put("1.0.1", STIX101);
            contentBindingMap.put("1.1", STIX11);
            contentBindingMap.put("1.1.1", STIX111);
            contentBindingMap.put("1.2", STIX12);
            contentBindingMap.put("10", STIX10);
            contentBindingMap.put("101", STIX101);
            contentBindingMap.put("11", STIX11);
            contentBindingMap.put("111", STIX111);
            contentBindingMap.put("12", STIX12);
        }
    }

    public enum Taxii11Headers {

        X_TAXII_Accept(Constants.HEADER_TAXII11_ACCEPT),
        X_TAXII_Protocol(Constants.HEADER_TAXII11_PROTOCOL),
        X_TAXII_Content_Type(Constants.HEADER_TAXII11_MESSAGE_BINDING),
        X_TAXII_Services(Constants.HEADER_TAXII11_SERVICES);

        final String key;
        final String value;

        Taxii11Headers(String value) {
            this.key = this.name().replace("_", "-");
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}
