package com.bcmc.xor.flare.client.api.service.scheduled.async;

import com.bcmc.xor.flare.client.TestData;
import com.bcmc.xor.flare.client.api.FlareclientApp;
import com.bcmc.xor.flare.client.api.domain.audit.EventType;
import com.bcmc.xor.flare.client.api.service.EventService;
import com.bcmc.xor.flare.client.api.service.StatusService;
import com.bcmc.xor.flare.client.api.service.TaxiiService;
import com.bcmc.xor.flare.client.taxii.taxii20.Taxii20Association;
import com.bcmc.xor.flare.client.taxii.taxii20.Taxii20RestTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlareclientApp.class)
public class AsyncStatusUpdateTest {

    private static final Logger log = LoggerFactory.getLogger(AsyncStatusUpdateTest.class);

    private AsyncStatusUpdateService asyncStatusUpdateService;

    @MockBean
    private StatusService statusService;

    @MockBean
    private EventService eventService;

    @Autowired
    private TaxiiService taxiiService;

    @MockBean
    private Taxii20RestTemplate taxii20RestTemplate;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        taxiiService.setTaxii20RestTemplate(taxii20RestTemplate);
        asyncStatusUpdateService = new AsyncStatusUpdateService(eventService, taxiiService, statusService);
    }

    @Test
    public void checkStatus() {
        when(statusService.getPending()).thenReturn(Collections.singletonList(TestData.taxii20Status));
        when(taxii20RestTemplate.getStatus(eq(TestData.taxii20Server), any())).thenReturn(TestData.taxii20Status);
        asyncStatusUpdateService.checkStatus();
        verify(eventService).createEvent(eq(EventType.STATUS_UPDATED), any(), any(Taxii20Association.class));
        verify(statusService).save(TestData.taxii20Status);

    }

}
