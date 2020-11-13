package com.bcmc.xor.flare.client.api.web.rest;

import com.bcmc.xor.flare.client.TestData;
import com.bcmc.xor.flare.client.api.domain.server.Taxii20Server;
import com.bcmc.xor.flare.client.api.service.CollectionService;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.api.service.TaxiiService;
import com.bcmc.xor.flare.client.error.ErrorConstants;
import com.bcmc.xor.flare.client.error.ManifestNotSupportedException;
import com.bcmc.xor.flare.client.taxii.TaxiiAssociation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(TaxiiAssociation.class)
@PowerMockIgnore("javax.management.*")
public class TaxiiManifestResourceTest {

    private TaxiiManifestResource taxiiManifestResource;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private ServerService serverService;

    @MockBean
    private CollectionService collectionService;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private TaxiiService taxiiService;

    private static Taxii20Server taxii20Server;
    private static String collectionId;

    @BeforeClass
    public static void setUp() {
        taxii20Server = TestData.taxii20Server;
        collectionId = TestData.taxii20Collection.getId();
    }

    @Before
    public void init() {
        taxiiManifestResource = new TaxiiManifestResource(taxiiService,serverService,collectionService);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void fetchManifestResource_NoFiltering() {

        when(serverService.getServerRepository().findOneByLabelIgnoreCase(any(String.class))).thenReturn(Optional.of(taxii20Server));
        PowerMockito.mockStatic(TaxiiAssociation.class);
        PowerMockito.when(TaxiiAssociation.from(any(String.class),any(String.class),any(ServerService.class), any(CollectionService.class))).thenReturn(TestData.taxii20Association);
        when(taxiiService.getTaxii20RestTemplate().getManifest(any(Taxii20Server.class),any(URI.class))).thenReturn(TestData.manifest);

        ResponseEntity<Map<String, String>> response = taxiiManifestResource.fetchManifestResource(taxii20Server.getLabel(),collectionId, null );
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey(collectionId));
        assertEquals(response.getBody().get(collectionId), TestData.manifest);
    }

    @Test
    public void fetchManifestResource_Filtering() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode filterObject = objectMapper.readTree(TestData.filterString);
        when(serverService.getServerRepository().findOneByLabelIgnoreCase(any(String.class))).thenReturn(Optional.of(taxii20Server));
        PowerMockito.mockStatic(TaxiiAssociation.class);
        PowerMockito.when(TaxiiAssociation.from(any(String.class),any(String.class),any(ServerService.class), any(CollectionService.class))).thenReturn(TestData.taxii20Association);
        when(taxiiService.getTaxii20RestTemplate().getManifest(any(Taxii20Server.class),any(URI.class))).thenReturn(TestData.manifest);

        ResponseEntity<Map<String, String>> response = taxiiManifestResource.fetchManifestResource(taxii20Server.getLabel(),collectionId, filterObject);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).containsKey(collectionId));
        assertFalse(response.getBody().isEmpty());
    }

    @Test(expected = ManifestNotSupportedException.class)
    public void fetchManifestResourceUnsupported() {
        when(serverService.getServerRepository().findOneByLabelIgnoreCase(any(String.class))).thenReturn(Optional.of(TestData.taxii11Server));

        ResponseEntity<Map<String, String>> response = taxiiManifestResource.fetchManifestResource(taxii20Server.getLabel(),collectionId, null);
        assertNotNull(response);
        assertEquals(response.getBody(), ErrorConstants.MANIFEST_NOT_SUPPORTED);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

}
