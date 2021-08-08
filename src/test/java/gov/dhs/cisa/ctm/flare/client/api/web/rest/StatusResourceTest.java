package gov.dhs.cisa.ctm.flare.client.api.web.rest;

import gov.dhs.cisa.ctm.flare.client.TestData;
import gov.dhs.cisa.ctm.flare.client.api.FlareclientApp;
import gov.dhs.cisa.ctm.flare.client.api.domain.status.Status;
import gov.dhs.cisa.ctm.flare.client.api.service.StatusService;
import gov.dhs.cisa.ctm.flare.client.api.service.dto.StatusDTO;
import gov.dhs.cisa.ctm.flare.client.api.web.rest.StatusResource;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlareclientApp.class)
public class StatusResourceTest {

    private StatusResource statusResource;

    @MockBean
    private StatusService statusService;

    private static Status status;
    private static StatusDTO statusDTO;
    private static List<StatusDTO> statusDTOList;
    private static Page<StatusDTO> statusPage;

    private static Pageable page = PageRequest.of(0, 10, Sort.by("id"));

    @BeforeClass
    public static void setUp() throws Exception {
        status = TestData.taxii21Status;
        statusDTO = new StatusDTO(status);
        statusDTOList = new ArrayList<>();
        statusDTOList.add(statusDTO);
        statusPage = new PageImpl<>(statusDTOList, page, 1);
    }

    @Before
    public void init() {
        statusResource = new StatusResource(statusService);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getStatus() {
        when(statusService.getStatusById(status.getId())).thenReturn(Optional.of(status));

        ResponseEntity<StatusDTO> response = statusResource.getStatus(status.getId());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        StatusDTO retrievedStatus = response.getBody();
        assertEquals(status.getId(), retrievedStatus.getId());
        assertEquals(status.getStatus(), retrievedStatus.getStatus());
        assertEquals(status.getUrl(), retrievedStatus.getUrl());
        assertEquals(status.getTotalCount(), retrievedStatus.getTotalCount());
    }

    @Test
    public void getAllStatus() {
        when(statusService.getAllStatus(page)).thenReturn(statusPage);

        ResponseEntity<List<StatusDTO>> response = statusResource.getAllStatus(page);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(statusDTO, response.getBody().get(0));

    }

}
