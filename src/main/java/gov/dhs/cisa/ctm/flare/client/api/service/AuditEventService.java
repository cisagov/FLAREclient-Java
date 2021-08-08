package gov.dhs.cisa.ctm.flare.client.api.service;

import gov.dhs.cisa.ctm.flare.client.api.config.audit.AuditEventConverter;
import gov.dhs.cisa.ctm.flare.client.api.repository.PersistenceAuditEventRepository;

import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Service for managing audit events.
 * <p>
 * This is the default implementation to support SpringBoot Actuator AuditEventRepository
 */
@Service
public class AuditEventService {

    private final PersistenceAuditEventRepository persistenceAuditEventRepository;

    private final AuditEventConverter auditEventConverter;

    public AuditEventService(
            PersistenceAuditEventRepository persistenceAuditEventRepository,
            AuditEventConverter auditEventConverter) {

        this.persistenceAuditEventRepository = persistenceAuditEventRepository;
        this.auditEventConverter = auditEventConverter;
    }

    public Page<AuditEvent> findAll(Pageable pageable) {
        return persistenceAuditEventRepository.findAll(pageable)
                .map(auditEventConverter::convertToAuditEvent);
    }

    public Page<AuditEvent> findByDates(LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        if (fromDate == null && toDate == null) {
            return persistenceAuditEventRepository.findAll(pageable)
                    .map(auditEventConverter::convertToAuditEvent);
        } else if (fromDate == null && toDate != null) {
            return persistenceAuditEventRepository.findAllByAuditEventDateBefore(toDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant(), pageable)
                    .map(auditEventConverter::convertToAuditEvent);
        } else if (fromDate != null && toDate == null) {
            return persistenceAuditEventRepository.findAllByAuditEventDateBetween(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                    Instant.now(), pageable)
                    .map(auditEventConverter::convertToAuditEvent);
        } else {
            return persistenceAuditEventRepository.findAllByAuditEventDateBetween(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                    toDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant(), pageable)
                    .map(auditEventConverter::convertToAuditEvent);
        }
    }

    public Optional<AuditEvent> find(String id) {
        return Optional.ofNullable(persistenceAuditEventRepository.findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(auditEventConverter::convertToAuditEvent);
    }
}
