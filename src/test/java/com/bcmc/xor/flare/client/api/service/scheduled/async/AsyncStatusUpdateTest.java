package com.bcmc.xor.flare.client.api.service.scheduled.async;

import com.bcmc.xor.flare.client.TestData;
import com.bcmc.xor.flare.client.api.FlareclientApp;
import com.bcmc.xor.flare.client.api.config.TraceConfiguration;
import com.bcmc.xor.flare.client.api.domain.audit.EventType;
import com.bcmc.xor.flare.client.api.domain.auth.User;
import com.bcmc.xor.flare.client.api.repository.UserRepository;
import com.bcmc.xor.flare.client.api.service.EventService;
import com.bcmc.xor.flare.client.api.service.StatusService;
import com.bcmc.xor.flare.client.api.service.TaxiiService;
import com.bcmc.xor.flare.client.taxii.taxii21.Taxii21Association;
import com.bcmc.xor.flare.client.taxii.taxii21.Taxii21RestTemplate;
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

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private Taxii21RestTemplate taxii21RestTemplate;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        taxiiService.setTaxii21RestTemplate(taxii21RestTemplate);
        asyncStatusUpdateService = new AsyncStatusUpdateService(eventService, taxiiService, statusService);
        asyncStatusUpdateService.setTraceConfiguration(new TraceConfiguration());
        asyncStatusUpdateService.setErrorFailCount(5);
    }

    @Test
    public void checkStatus() {
        when(statusService.getPending()).thenReturn(Collections.singletonList(TestData.taxii21Status));
        when(taxii21RestTemplate.getStatus(eq(TestData.taxii21Server), any())).thenReturn(TestData.taxii21Status);
        asyncStatusUpdateService.checkStatus();
        verify(eventService).createEvent(eq(EventType.STATUS_UPDATED), any(), any(Taxii21Association.class));
        verify(statusService).save(TestData.taxii21Status);

    }

}
