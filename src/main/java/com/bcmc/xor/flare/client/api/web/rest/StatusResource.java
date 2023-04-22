package com.bcmc.xor.flare.client.api.web.rest;

import com.bcmc.xor.flare.client.api.service.StatusService;
import com.bcmc.xor.flare.client.api.service.dto.StatusDTO;
import com.bcmc.xor.flare.client.util.PaginationUtil;
import com.bcmc.xor.flare.client.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
class StatusResource {

    private static final Logger log = LoggerFactory.getLogger(StatusResource.class);

    private final StatusService statusService;

    public StatusResource(StatusService statusService) {
        this.statusService = statusService;
    }

    /**
     * GET /status : get all status.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and with body all status
     */
    @GetMapping("/status")
    public ResponseEntity<List<StatusDTO>> getAllStatus(Pageable pageable) {
        log.debug("REST request to get all statuses");
        final Page<StatusDTO> page = statusService.getAllStatus(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/status");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    /**
     * GET /status/:id : get the "status" of the object.
     *
     * @param id the status of the object to find
     * @return the ResponseEntity with status 200 (OK) and with body the "status" object, or with status 404 (Not Found)
     */
    @GetMapping("/status/{id}")
    public ResponseEntity<StatusDTO> getStatus(@PathVariable String id) {
        log.debug("REST request to get Status : {}", id);
        return ResponseUtil.wrapOrNotFound(statusService.getStatusById(id).map(StatusDTO::new));
    }
    /**
     * GET /status/:id : get the "status" of the object.
     *
     * @param id the status of the object to find
     * @return the ResponseEntity with status 200 (OK) and with body the "status" object, or with status 404 (Not Found)
     */
    @GetMapping("/status/{id}/clear")
    public ResponseEntity clearErrorCountForStatus(@PathVariable String id) {
        log.info("REST request to clear errors for Status : {}", id);
        try {
            if (statusService.clearErrorCount(id)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception ex) {
            log.error("[x] Error: failed to clear errors for Status: {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
