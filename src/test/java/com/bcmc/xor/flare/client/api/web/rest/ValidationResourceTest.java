package com.bcmc.xor.flare.client.api.web.rest;

import com.bcmc.xor.flare.client.TestData;
import com.bcmc.xor.flare.client.api.FlareclientApp;
import com.bcmc.xor.flare.client.api.domain.content.Stix1ContentWrapper;
import com.bcmc.xor.flare.client.api.domain.parameters.UploadedFile;
import com.bcmc.xor.flare.client.taxii.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlareclientApp.class)
public class ValidationResourceTest {

    @Autowired
    private ValidationResource validationResource;

    @MockBean
    Validation validation;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Validation.setInstance(validation);
    }

    @Test
    public void emptyValidationTest() {
        assertEquals(HttpStatus.BAD_REQUEST, validationResource.validateStixFile(new HashMap<>()).getStatusCode());
    }

    @Test
    public void positiveValidationTest() {

        Stix1ContentWrapper contentWrapper = new Stix1ContentWrapper(TestData.rawStix111, TestData.taxii11Association);
        Map<String, List<String>> results = new HashMap<>();
        results.put(contentWrapper.getId(), new ArrayList<>());
        Map.Entry<String, List<String>> errorsEntry = results.entrySet().iterator().next();
        when(validation.validateAndReturnErrors(TestData.rawStix111)).thenReturn(errorsEntry);

        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setContent(TestData.rawStix111);
        uploadedFile.setFilename("test.xml");
        uploadedFile.setHash(1);

        Map<Integer, UploadedFile> listOfContent = new HashMap<>(2);
        listOfContent.put(1, uploadedFile);

        ResponseEntity<Map<Integer, List<String>>> response = validationResource.validateStixFile(listOfContent);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get(1));
        List errors = response.getBody().get(1);
        assertTrue("This STIX file should have no validation errors", errors.isEmpty());
    }

    @Test
    public void negativeValidationTest() {

        Stix1ContentWrapper contentWrapper = new Stix1ContentWrapper(TestData.rawStix111, TestData.taxii11Association);
        Map<String, List<String>> results = new HashMap<>();

        results.put(contentWrapper.getId(), Collections.singletonList("Error!"));
        Map.Entry<String, List<String>> errorsEntry = results.entrySet().iterator().next();
        when(validation.validateAndReturnErrors(TestData.rawStix111)).thenReturn(errorsEntry);

        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setContent(TestData.rawStix111);
        uploadedFile.setFilename("test.xml");
        uploadedFile.setHash(1);

        Map<Integer, UploadedFile> listOfContent = new HashMap<>(2);
        listOfContent.put(1, uploadedFile);

        ResponseEntity<Map<Integer, List<String>>> response = validationResource.validateStixFile(listOfContent);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get(1));
        List errorsResponse = response.getBody().get(1);
        assertFalse("This STIX file should have validation errors", errorsResponse.isEmpty());
        assertEquals(errorsResponse.get(0), "Error!");
    }
}
