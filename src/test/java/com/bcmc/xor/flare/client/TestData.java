package com.bcmc.xor.flare.client;

import com.bcmc.xor.flare.client.api.config.Constants;
import com.bcmc.xor.flare.client.api.domain.async.AsyncFetch;
import com.bcmc.xor.flare.client.api.domain.async.Taxii11AsyncFetch;
import com.bcmc.xor.flare.client.api.domain.audit.Event;
import com.bcmc.xor.flare.client.api.domain.auth.Authority;
import com.bcmc.xor.flare.client.api.domain.auth.User;
import com.bcmc.xor.flare.client.api.domain.collection.Taxii11Collection;
import com.bcmc.xor.flare.client.api.domain.collection.Taxii21Collection;
import com.bcmc.xor.flare.client.api.domain.parameters.Taxii11PollParameters;
import com.bcmc.xor.flare.client.api.domain.parameters.Taxii21GetParameters;
import com.bcmc.xor.flare.client.api.domain.server.ApiRoot;
import com.bcmc.xor.flare.client.api.domain.server.Taxii11Server;
import com.bcmc.xor.flare.client.api.domain.server.Taxii21Server;
import com.bcmc.xor.flare.client.api.domain.status.Status;
import com.bcmc.xor.flare.client.api.security.AuthoritiesConstants;
import com.bcmc.xor.flare.client.api.service.UserService;
import com.bcmc.xor.flare.client.api.service.dto.UserDTO;
import com.bcmc.xor.flare.client.taxii.taxii11.Taxii11Association;
import com.bcmc.xor.flare.client.taxii.taxii21.Taxii21Association;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import org.mitre.taxii.messages.xml11.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import xor.bcmc.taxii2.JsonHandler;
import xor.bcmc.taxii2.resources.Collection;
import xor.bcmc.taxii2.resources.Discovery;
import xor.flare.utils.crypto.PasswordUtil;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static org.mockito.Mockito.when;

public class TestData {


    public static User user;
    public static Taxii11Server taxii11Server;
    public static Taxii21Server taxii21Server;
    public static Taxii11Collection taxii11Collection;
    public static Taxii21Collection taxii21Collection;
    public static ApiRoot apiRoot;
    public static Taxii11Association taxii11Association;
    public static Taxii21Association taxii21Association;
    public static AsyncFetch taxii11TestRequest;
    public static CollectionRecordType collectionRecordType;
    public static ServiceContactInfoType pollingServices;
    public static InboxServiceBindingsType inboxServiceBindingsType;
    public static ServiceInstanceType pollServiceInstance;
    public static ServiceInstanceType inboxServiceInstance;
    public static ServiceInstanceType collectionManagementServiceInstance;
    public static ServiceInstanceType discoveryServiceInstance;
    public static PollResponse pollResponse;
    public static String rawStix111;
    public static String rawStix111Invalid;
    public static String rawStix21;
    public static JsonElement jsonStix21;
    public static ContentBlock contentBlock;
    public static Taxii11PollParameters pollParameters;
    public static Taxii21GetParameters getParameters;
    public static StatusMessage failureStatusMessage;
    public static StatusMessage successStatusMessage;
    public static ResponseEntity<String> taxii21GetResponse;
    public static Event event;
    public static DiscoveryResponse discoveryResponse;
    public static Discovery taxii21Discovery;
    public static xor.bcmc.taxii2.resources.ApiRoot taxii21ApiRoot;
    public static Collection taxii21CollectionObject;
    public static CollectionInformationResponse collectionInformationResponse;
    public static Status taxii21Status;
    public static String manifest;
    public static String filterStringKey;
    public static String filterStringValue;

    static {
        user = new User();
        Authority authority = new Authority();
        authority.setName("user1");
        Set<Authority> authorities = Sets.newHashSet(authority);
        Map<String, String> serverCredentialsMap = new HashMap<>();
        String encryptedUser = PasswordUtil.getEncryptedPassword("user1", "flare1234");
        serverCredentialsMap.put("username",encryptedUser);
        String encryptedPassword = PasswordUtil.getEncryptedPassword("password", "flare1234");
        serverCredentialsMap.put("password", encryptedPassword);
        user.setId(UUID.randomUUID().toString());
        user.setAuthorities(authorities);
        user.setEmail("user@mail.com");
        user.setLogin("user1");
        user.setActivated(true);
        user.setPassword("flare1234");
        user.setFirstName("User");
        user.setLastName("User");
        user.setServerCredentials(serverCredentialsMap);

        // Establish test data: Taxii11Server
        taxii11Server = new Taxii11Server();
        taxii11Server.setId(UUID.randomUUID().toString());
        taxii11Server.setVersion(Constants.TaxiiVersion.TAXII11);
        taxii11Server.setLabel("Taxii11");
        taxii11Server.setUrl(URI.create("http://localhost:5000/discovery/"));

        // Establish test data: Taxii11Collection
        pollingServices =
                new ServiceContactInfoType(Constants.HEADER_TAXII11_PROTOCOL,
                        "http://localhost:5000/poll/",
                        Collections.singletonList(Constants.HEADER_TAXII11_SERVICES));

        inboxServiceBindingsType = new InboxServiceBindingsType()
                .withAddress("test")
                .withMessageBindings(Constants.HEADER_TAXII11_MESSAGE_BINDING)
                .withContentBindings(Constants.stix1ContentBindingsMap.values());

        pollServiceInstance = new ServiceInstanceType()
                .withServiceType(ServiceTypeEnum.POLL)
                .withAddress("test")
                .withAvailable(true)
                .withMessageBindings(Constants.HEADER_TAXII11_MESSAGE_BINDING)
                .withContentBindings(Constants.stix1ContentBindingsMap.values());

        inboxServiceInstance = new ServiceInstanceType()
                .withServiceType(ServiceTypeEnum.INBOX)
                .withAddress("test")
                .withAvailable(true)
                .withMessageBindings(Constants.HEADER_TAXII11_MESSAGE_BINDING)
                .withContentBindings(Constants.stix1ContentBindingsMap.values());

        collectionManagementServiceInstance = new ServiceInstanceType()
                .withServiceType(ServiceTypeEnum.COLLECTION_MANAGEMENT)
                .withAddress("test")
                .withAvailable(true)
                .withMessageBindings(Constants.HEADER_TAXII11_MESSAGE_BINDING)
                .withContentBindings(Constants.stix1ContentBindingsMap.values());

        discoveryServiceInstance = new ServiceInstanceType()
                .withServiceType(ServiceTypeEnum.DISCOVERY)
                .withAddress("test")
                .withAvailable(true)
                .withMessageBindings(Constants.HEADER_TAXII11_MESSAGE_BINDING)
                .withContentBindings(Constants.stix1ContentBindingsMap.values());


        collectionRecordType = new CollectionRecordType()
                .withCollectionType(CollectionTypeEnum.DATA_FEED)
                .withDescription("Collection record under test")
                .withCollectionName("Test11Collection")
                .withAvailable(true)
                .withPollingServices(pollingServices)
                .withReceivingInboxServices(inboxServiceBindingsType);

        taxii11Collection = new Taxii11Collection(collectionRecordType, taxii11Server.getId());
        taxii11Collection.setId(UUID.randomUUID().toString());
        taxii11Collection.setTaxiiVersion(Constants.TaxiiVersion.TAXII11);
        taxii11Collection.setId(UUID.randomUUID().toString());
        taxii11Collection.setDisplayName("Test11Collection");



        // Establish test data: Taxii21Server
        taxii21Server = new Taxii21Server();
        taxii21Server.setVersion(Constants.TaxiiVersion.TAXII21);
        taxii21Server.setLabel("Test TAXII 2.1 Server");
        taxii21Server.setUrl(URI.create("http://localhost:5000/taxii/"));

        // Manifest
        manifest = "{\"more\": false, \"objects\": [{\"date_added\": \"2020-10-21T20:09:10.331994Z\", " +
                "\"id\": \"marking-definition--5e57c739-391a-4eb3-b6be-7d15ca92d5ed\", \"media_type\": " +
                "\"application/stix+json;version=2.1\", \"version\": \"2017-01-20T00:00:00.000Z\"}, " +
                "{\"date_added\": \"2020-10-21T20:09:10.331994Z\", \"id\": \"identity--f8d34e5b-fbf0-451c-9fa3-d1312372bef2\", " +
                "\"media_type\": \"application/stix+json;version=2.1\", \"version\": \"2020-06-14T02:04:17.000Z\"}, " +
                "{\"date_added\": \"2020-10-21T20:09:10.331994Z\", \"id\": \"attack-pattern--592c0542-45e3-474a-84c7-e6a705547ce2\", " +
                "\"media_type\": \"application/stix+json;version=2.1\", \"version\": \"2021-04-04T09:29:35.000Z\"}]}";
        filterStringKey = "match[id]=";
        filterStringValue = "marking-definition--5e57c739-391a-4eb3-b6be-7d15ca92d5ed";

        // Establish test data: Taxii21 ApiRoot
        xor.bcmc.taxii2.resources.ApiRoot apiRootObject = new xor.bcmc.taxii2.resources.ApiRoot()
                .withTitle("Test Title")
                .withDescription("Test Description")
                .withMaxContentLength(10000000)
                .withVersions(Collections.singletonList("taxii-2.1"))
                .withId("TestApi");

        apiRoot = new ApiRoot(taxii21Server.getId());
        String apiRootId = UUID.randomUUID().toString();
        apiRoot.setId(apiRootId);
        apiRoot.setObject(apiRootObject);
        apiRoot.setCollections(Sets.newHashSet(taxii21Collection));
        apiRoot.setUrl(URI.create("http://localhost:5000/TestApi/"));
        apiRoot.setEndpoint("TestApi");

        // Establish test data: Taxii21Collection

        Collection collectionObject = new Collection(UUID.randomUUID().toString(), "Taxii21Collection", true, true);
        taxii21Collection = new Taxii21Collection(collectionObject, taxii21Server.getId(), apiRoot.getEndpoint());
        taxii21Collection.setTaxiiVersion(Constants.TaxiiVersion.TAXII21);
        taxii21Collection.setId(UUID.randomUUID().toString());
        taxii21Collection.setDisplayName("Taxii21Collection");

        taxii11Server.setServiceInstances(Sets.newHashSet(pollServiceInstance, inboxServiceInstance, discoveryServiceInstance, collectionManagementServiceInstance));
        taxii11Server.setCollections(Sets.newHashSet(taxii11Collection));
        taxii21Server.setCollections(Sets.newHashSet(taxii21Collection));

        taxii11Association = new Taxii11Association(taxii11Server, taxii11Collection, user);
        taxii21Association = new Taxii21Association(taxii21Server, taxii21Collection, user);

        rawStix111 = "<stix:STIX_Package\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xmlns:stix=\"http://stix.mitre.org/stix-1\"\n" +
                "    xmlns:indicator=\"http://stix.mitre.org/Indicator-2\"\n" +
                "    xmlns:cybox=\"http://cybox.mitre.org/cybox-2\"\n" +
                "    xmlns:AddressObject=\"http://cybox.mitre.org/objects#AddressObject-2\"\n" +
                "    xmlns:stixVocabs=\"http://stix.mitre.org/default_vocabularies-1\"\n" +
                "    xmlns:example=\"http://example.com/\"\n" +
                "    xsi:schemaLocation=\"http://stix.mitre.org/stix-1 ../stix_core.xsd\n" +
                "    http://stix.mitre.org/Indicator-2 ../indicator.xsd\n" +
                "\n" +
                "    http://stix.mitre.org/default_vocabularies-1 ../stix_default_vocabularies.xsd\n" +
                "    http://cybox.mitre.org/objects#AddressObject-2 ../cybox/objects/Address_Object.xsd\"\n" +
                "    id=\"example:STIXPackage-33fe3b22-0201-47cf-85d0-97c02164528d\"\n" +
                "    timestamp=\"2014-05-08T09:00:00.000000Z\"\n" +
                "    version=\"1.1.1\"\n" +
                ">\n" +
                "    <stix:STIX_Header>\n" +
                "        <stix:Title>Example watchlist that contains IP information.</stix:Title>\n" +
                "        <stix:Package_Intent xsi:type=\"stixVocabs:PackageIntentVocab-1.0\">Indicators - Watchlist</stix:Package_Intent>\n" +
                "    </stix:STIX_Header>\n" +
                "    <stix:Indicators>\n" +
                "        <stix:Indicator xsi:type=\"indicator:IndicatorType\" id=\"example:Indicator-33fe3b22-0201-47cf-85d0-97c02164528d\" timestamp=\"2014-05-08T09:00:00.000000Z\">\n" +
                "            <indicator:Type xsi:type=\"stixVocabs:IndicatorTypeVocab-1.1\">IP Watchlist</indicator:Type>\n" +
                "            <indicator:Description>Sample IP Address Indicator for this watchlist. This contains one indicator with a set of three IP addresses in the watchlist.</indicator:Description>\n" +
                "            <indicator:Observable  id=\"example:Observable-1c798262-a4cd-434d-a958-884d6980c459\">\n" +
                "                <cybox:Object id=\"example:Object-1980ce43-8e03-490b-863a-ea404d12242e\">\n" +
                "                    <cybox:Properties xsi:type=\"AddressObject:AddressObjectType\" category=\"ipv4-addr\">\n" +
                "                        <AddressObject:Address_Value condition=\"Equals\" apply_condition=\"ANY\">10.0.0.0##comma##10.0.0.1##comma##10.0.0.2</AddressObject:Address_Value>\n" +
                "                    </cybox:Properties>\n" +
                "                </cybox:Object>\n" +
                "            </indicator:Observable>\n" +
                "        </stix:Indicator>\n" +
                "    </stix:Indicators>\n" +
                "</stix:STIX_Package>\n";

        rawStix111Invalid = "<stix:STIX_Package\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xmlns:stix=\"http://stix.mitre.org/stix-1\"\n" +
                "    xmlns:indicator=\"http://stix.mitre.org/Indicator-2\"\n" +
                "    xmlns:cybox=\"http://cybox.mitre.org/cybox-2\"\n" +
                "    xmlns:AddressObject=\"http://cybox.mitre.org/objects#AddressObject-2\"\n" +
                "    xmlns:stixVocabs=\"http://stix.mitre.org/default_vocabularies-1\"\n" +
                "    xmlns:example=\"http://example.com/\"\n" +
                "    xsi:schemaLocation=\"http://stix.mitre.org/stix-1 ../stix_core.xsd\n" +
                "    http://stix.mitre.org/Indicator-2 ../indicator.xsd\n" +
                "\n" +
                "    http://stix.mitre.org/default_vocabularies-1 ../stix_default_vocabularies.xsd\n" +
                "    http://cybox.mitre.org/objects#AddressObject-2 ../cybox/objects/Address_Object.xsd\"\n" +
                "    id=\"example:STIXPackage-33fe3b22-0201-47cf-85d0-97c02164528d\"\n" +
                "    timestamp=\"2014-05-08T09:00:00.000000Z\"\n" +
                "    version=\"1.1.1\"\n" +
                ">\n" +
                "    <stix:STIX_Header>\n" +
                "        <stix:Title>Example watchlist that contains IP information.</stix:Title>\n" +
                "        <stix:Package_Intent xsi:type=\"stixVocabs:PackageIntentVocab-1.0\">Indicators - Watchlist</stix:Package_Intent>\n" +
                "    </stix:STIX_Header>\n" +
                "    <stix:Indicators>\n" +
                "        <stix:Indicator xsi:type=\"indicator:IndicatorType\" id=\"example:Indicator-33fe3b22-0201-47cf-85d0-97c02164528d\" timestamp=\"2014-05-08T09:00:00.000000Z\">\n" +
                "            <indicator:Observable xsi:type=\"stixVocabs:IndicatorTypeVocab-1.1\">IP Watchlist</indicator:Observable>\n" +
                "            <indicator:Description>Sample IP Address Indicator for this watchlist. This contains one indicator with a set of three IP addresses in the watchlist.</indicator:Description>\n" +
                "            <indicator:Observable  id=\"example:Observable-1c798262-a4cd-434d-a958-884d6980c459\">\n" +
                "                <cybox:Object id=\"example:Object-1980ce43-8e03-490b-863a-ea404d12242e\">\n" +
                "                    <cybox:Properties xsi:type=\"AddressObject:AddressObjectType\" category=\"ipv4-addr\">\n" +
                "                        <AddressObject:Address_Value condition=\"Equals\" apply_condition=\"ANY\">10.0.0.0##comma##10.0.0.1##comma##10.0.0.2</AddressObject:Address_Value>\n" +
                "                    </cybox:Properties>\n" +
                "                </cybox:Object>\n" +
                "            </indicator:Observable>\n" +
                "        </stix:Indicator>\n" +
                "    </stix:Indicators>\n" +
                "</stix:STIX_Package>\n";

        // Establish test data: Taxii11 Fetch Params
        Instant begin = Instant.now().minusMillis(60000*3);
        Instant end = Instant.now();

        pollParameters = new Taxii11PollParameters();
        pollParameters.setAssociation(taxii11Association);
        pollParameters.setFetchUrl(URI.create("http://localhost:5000/poll/"));
        pollParameters.setId(UUID.randomUUID().toString());
        pollParameters.setServerLabel(taxii11Server.getLabel());
        pollParameters.setRepeat(false);
        pollParameters.setWindow(8);
        pollParameters.setStartDate(ZonedDateTime.ofInstant(begin, ZoneId.of("UTC")));
        pollParameters.setEndDate(ZonedDateTime.ofInstant(end, ZoneId.of("UTC")));
        pollParameters.setContentBindings(new ArrayList<>(Constants.ContentBindings.contentBindingMap.values()));

        // Establish test data: Taxii11 Async Request
        taxii11TestRequest = new Taxii11AsyncFetch(pollParameters, 1000000);

        XMLGregorianCalendarImpl xmlGregorianCalendar = (XMLGregorianCalendarImpl) XMLGregorianCalendarImpl.createDateTime(2018, 1, 1, 1, 1, 1);
        contentBlock = new ContentBlock()
                .withContentBinding(new ContentInstanceType(null, Constants.stix1ContentBindingsStringMap.get("1.1.1")))
                .withTimestampLabel(xmlGregorianCalendar)
                .withContent(new AnyMixedContentType().withContent(rawStix111));

        pollResponse = new PollResponse()
                .withContentBlocks(contentBlock)
                .withCollectionName(taxii11Collection.getName())
                .withMessageId(UUID.randomUUID().toString())
                .withInResponseTo(UUID.randomUUID().toString())
                .withMore(false)
                .withInclusiveEndTimestamp(xmlGregorianCalendar);

        failureStatusMessage = new StatusMessage()
                .withStatusType(StatusTypeEnum.FAILURE.toString())
                .withMessage("Failure!")
                .withMessageId(UUID.randomUUID().toString())
                .withInResponseTo(UUID.randomUUID().toString());

        successStatusMessage = new StatusMessage()
                .withStatusType(StatusTypeEnum.SUCCESS.toString())
                .withMessage("Success!")
                .withMessageId(UUID.randomUUID().toString())
                .withInResponseTo(UUID.randomUUID().toString());

        getParameters = new Taxii21GetParameters();
        getParameters.setAssociation(taxii21Association);
        getParameters.setApiRootRef(apiRootId);
        getParameters.setServerLabel(taxii21Server.getLabel());
        getParameters.setAddedAfter(ZonedDateTime.now().minusDays(1));
        getParameters.setFetchUrl(URI.create("http://test.com"));
        getParameters.setRepeat(false);
        getParameters.setWindow(8000);
        //getParameters.setTypes(Constants.STIX20_TYPES.toArray(new String[]{}));

        rawStix21 = "{\n" +
                "  \"type\": \"bundle\",\n" +
                "  \"id\": \"bundle--c6a895f2-849c-4d1b-aba4-4b45c2800374\",\n" +
                "  \"spec_version\": \"2.0\",\n" +
                "  \"objects\": [\n" +
                "    {\n" +
                "      \"type\": \"identity\",\n" +
                "      \"id\": \"identity--39012926-a052-44c4-ae48-caaf4a10ee61\",\n" +
                "      \"created\": \"2017-02-24T15:50:10.564Z\",\n" +
                "      \"modified\": \"2017-08-24T15:50:10.564Z\",\n" +
                "      \"name\": \"Alpha Threat Analysis Org.\",\n" +
                "      \"identity_class\": \"organization\",\n" +
                "      \"labels\": [\n" +
                "        \"Cyber Security\"\n" +
                "      ],\n" +
                "      \"sectors\": [\n" +
                "        \"technology\"\n" +
                "      ],\n" +
                "      \"contact_information\": \"info@alpha.org\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"identity\",\n" +
                "      \"id\": \"identity--5206ba14-478f-4b0b-9a48-395f690c20a1\",\n" +
                "      \"created\": \"2017-02-26T17:55:10.442Z\",\n" +
                "      \"modified\": \"2017-02-26T17:55:10.442Z\",\n" +
                "      \"name\": \"Beta Cyber Intelligence Company\",\n" +
                "      \"identity_class\": \"organization\",\n" +
                "      \"labels\": [\n" +
                "        \"Cyber Security\"\n" +
                "      ],\n" +
                "      \"sectors\": [\n" +
                "        \"technology\"\n" +
                "      ],\n" +
                "      \"contact_information\": \"info@beta.com\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"indicator\",\n" +
                "      \"id\": \"indicator--9299f726-ce06-492e-8472-2b52ccb53192\",\n" +
                "      \"created_by_ref\": \"identity--39012926-a052-44c4-ae48-caaf4a10ee6e\",\n" +
                "      \"created\": \"2017-02-27T13:57:10.515Z\",\n" +
                "      \"modified\": \"2017-02-27T13:57:10.515Z\",\n" +
                "      \"name\": \"Malicious URL\",\n" +
                "      \"description\": \"This URL is potentially associated with malicious activity and is listed on several blacklist sites.\",\n" +
                "      \"pattern\": \"[url:value = 'http://paypa1.banking.com']\",\n" +
                "      \"valid_from\": \"2015-06-29T09:10:15.915Z\",\n" +
                "      \"labels\": [\n" +
                "        \"malicious-activity\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"sighting\",\n" +
                "      \"id\": \"sighting--8356e820-8080-4692-aa91-ecbe94006832\",\n" +
                "      \"created_by_ref\": \"identity--5206ba14-478f-4b0b-9a48-395f690c20a2\",\n" +
                "      \"created\": \"2017-02-28T19:37:11.213Z\",\n" +
                "      \"modified\": \"2017-02-28T19:37:11.213Z\",\n" +
                "      \"first_seen\": \"2017-02-27T21:37:11.213Z\",\n" +
                "      \"last_seen\": \"2017-02-27T21:37:11.213Z\",\n" +
                "      \"count\": 1,\n" +
                "      \"sighting_of_ref\": \"indicator--9299f726-ce06-492e-8472-2b52ccb53191\",\n" +
                "      \"where_sighted_refs\": [\n" +
                "        \"identity--5206ba14-478f-4b0b-9a48-395f690c20a2\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        taxii21GetResponse = ResponseEntity.ok(rawStix21);

        jsonStix21 = JsonHandler.getInstance().getGson().fromJson(rawStix21, JsonElement.class);


        event = new Event();
        event.setId(UUID.randomUUID().toString());
        event.setDetails("test");
        event.setServer(taxii11Server.getLabel());
        event.setTaxiiCollection(taxii11Collection.getDisplayName());

        discoveryResponse = new DiscoveryResponse()
                .withMessageId(UUID.randomUUID().toString())
                .withInResponseTo(UUID.randomUUID().toString())
                .withServiceInstances(discoveryServiceInstance, pollServiceInstance, collectionManagementServiceInstance, inboxServiceInstance);

        collectionInformationResponse = new CollectionInformationResponse()
                .withCollections(collectionRecordType)
                .withMessageId(UUID.randomUUID().toString())
                .withInResponseTo(UUID.randomUUID().toString());

        taxii21Discovery = new Discovery()
                .withId(UUID.randomUUID().toString())
                .withTitle("Test")
                .withContact("Test")
                .withDefaultApiRoot("test")
                .withApiRoots(Collections.singletonList("test"))
                .withDescription("Test");
        taxii21Server.updateFromDiscovery(taxii21Discovery);

        taxii21ApiRoot = new xor.bcmc.taxii2.resources.ApiRoot()
                .withId(UUID.randomUUID().toString())
                .withTitle("Test")
                .withDescription("Test")
                .withMaxContentLength(1000000000)
                .withVersions(Collections.singletonList(Constants.HEADER_TAXII21_JSON));
        HashSet<ApiRoot> apiRootObjects = new HashSet<>();
        apiRootObjects.add(apiRoot);
        taxii21Server.setApiRootObjects(apiRootObjects);
        taxii21Collection.setApiRootRef(apiRoot.getEndpoint());

        taxii21CollectionObject = new Collection()
                .withId(UUID.randomUUID().toString())
                .withTitle("Test")
                .withDescription("Test")
                .withMediaTypes(Collections.singletonList(Constants.HEADER_STIX21_JSON))
                .withCanRead(true)
                .withCanWrite(true);

        taxii21Status = new Status();
        taxii21Status.setAssociation(taxii21Association);
        taxii21Status.setFailureCount(0);
        taxii21Status.setPendingCount(0);
        taxii21Status.setSuccessCount(5);
        taxii21Status.setTotalCount(5);
        taxii21Status.setStatus("COMPLETE");
        taxii21Status.setRequestTimestamp(ZonedDateTime.now().minusDays(1));

        taxii21Association.setApiRoot(apiRoot);

    }

    public static void setLoggedInUser(SecurityContext securityContext, UserService userService) {
        userService.createUser(new UserDTO(TestData.user));
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(AuthoritiesConstants.ADMIN);
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(TestData.user.getLogin(), TestData.user.getPassword(), Collections.singletonList(grantedAuthority));
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, TestData.user.getPassword());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

}