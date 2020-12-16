package com.bcmc.xor.flare.client.api.web.rest;

import com.bcmc.xor.flare.client.api.service.AuditEventService;
import com.bcmc.xor.flare.client.util.PaginationUtil;
import com.bcmc.xor.flare.client.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for getting the audit events.
 */
@RestController
@Validated
@RequestMapping("/management/audits")
public class AuditResource {

    private static final Logger log = LoggerFactory.getLogger(AuditResource.class);

    private final AuditEventService auditEventService;

    public AuditResource(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    /**
     * GET  /audits : get a page of AuditEvents between the fromDate and toDate.
     *
     * @param fromDate the start of the time period of AuditEvents to get
     * @param toDate the end of the time period of AuditEvents to get
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of AuditEvents in body
     */
    @GetMapping
    public ResponseEntity<List<AuditEvent>> getByDates(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            Pageable pageable) {
        log.debug("REST request to get AuditEvents between {} and {}", fromDate, toDate);

        Page<AuditEvent> page = auditEventService.findByDates(fromDate, toDate, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/management/audits");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /audits/:id : get an AuditEvent by id.
     *
     * @param id the id of the entity to get
     * @return the ResponseEntity with status 200 (OK) and the AuditEvent in body, or status 200 with an empty body
     */
    @GetMapping("/{id:.+}")
    public ResponseEntity<AuditEvent> get(@PathVariable String id) {
        log.debug("REST request to get AuditEvent for id: {}", id);
        return ResponseUtil.wrapOrNotFound(auditEventService.find(id));
    }
}
