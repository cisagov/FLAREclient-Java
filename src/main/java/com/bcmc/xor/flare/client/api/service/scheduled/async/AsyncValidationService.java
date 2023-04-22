package com.bcmc.xor.flare.client.api.service.scheduled.async;

import com.bcmc.xor.flare.client.api.domain.audit.EventType;
import com.bcmc.xor.flare.client.api.domain.content.Stix1ContentWrapper;
import com.bcmc.xor.flare.client.api.domain.content.ValidationResult;
import com.bcmc.xor.flare.client.api.repository.ContentRepository;
import com.bcmc.xor.flare.client.api.service.EventService;
import com.bcmc.xor.flare.client.taxii.taxii11.Taxii11Association;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A service for asynchronously validating STIX 1.X content
 */
@SuppressWarnings("unused")
@Service
public class AsyncValidationService {

    private static final Logger log = LoggerFactory.getLogger(AsyncValidationService.class);
    private final ContentRepository contentRepository;
    private final EventService eventService;

    public AsyncValidationService(ContentRepository contentRepository, EventService eventService) {
        this.contentRepository = contentRepository;
        this.eventService = eventService;
    }

    @Async("taskExecutor")
    @Scheduled(fixedRateString = "${flare.validation-interval}")
    public synchronized void validatePending() {
        List<String> details = new ArrayList<>();
        List<Stix1ContentWrapper> listOfContent = contentRepository.findAllPending11Content();
        if (!listOfContent.isEmpty()) {
            listOfContent.forEach(content -> content.setValidationStatus(ValidationResult.Status.VALIDATING));
            contentRepository.saveAll(listOfContent);
            log.debug("Found {} pending content wrappers", listOfContent.size());

            // Validation results will be stored in a map that uses the server label and collection label as the key, with the result counts as the values
            // Because of this, we can create specific server-collection events for specific validation results
            Map<Taxii11Association, ValidationCountResult> validationResultMap = new HashMap<>();
            for (Stix1ContentWrapper content : listOfContent) {
                Taxii11Association key = (Taxii11Association) content.getAssociation();
                ValidationResult result = content.validate();
                // Update the results count map based on the ValidationResult
                switch (result.getStatus()) {
                    case VALID:
                        validationResultMap.compute(key, (k, v) -> (v == null) ? new ValidationCountResult(1, 0, 0, 0) : v.add(1, 0, 0, 0));
                        break;
                    case INVALID:
                        validationResultMap.compute(key, (k, v) -> (v == null) ? new ValidationCountResult(0, 1, 0, 0) : v.add(0, 1, 0, 0));
                        details.addAll(result.getErrors());
                        break;
                    case PENDING:
                        validationResultMap.compute(key, (k, v) -> (v == null) ? new ValidationCountResult(0, 0, 1, 0) : v.add(0, 0, 1, 0));
                        break;
                    case ERROR:
                        validationResultMap.compute(key, (k, v) -> (v == null) ? new ValidationCountResult(0, 0, 0, 1) : v.add(0, 0, 0, 1));
                        break;
                }
                contentRepository.save(content);

            }

            for (Map.Entry<Taxii11Association, ValidationCountResult> validationResult : validationResultMap.entrySet()) {
                String feedback = String.format("Validated %d pieces of content for server '%s' and collection '%s'. Valid: %d. Invalid: %d. Pending: %d. Errors: %d. %s",
                    listOfContent.size(),
                    validationResult.getKey().getServer().getLabel(),
                    validationResult.getKey().getCollection().getDisplayName(),
                    validationResult.getValue().getValid(),
                    validationResult.getValue().getInvalid(),
                    validationResult.getValue().getPending(),
                    validationResult.getValue().getErrors(),
                    details.isEmpty() || details.size() < 1000 ? "" : "Details: " + String.join(", ", details));
                log.info(feedback);
                eventService.createEvent(EventType.VALIDATION, feedback, validationResult.getKey());
            }

        }
    }

    static class ValidationCountResult {

        private int valid;
        private int invalid;
        private int pending;
        private int errors;

        ValidationCountResult(int valid, int invalid, int pending, int errors) {
            this.valid = valid;
            this.invalid = invalid;
            this.pending = pending;
            this.errors = errors;
        }

        ValidationCountResult add(int valid, int invalid, int pending, int errors) {
            this.valid += valid;
            this.invalid += invalid;
            this.pending += pending;
            this.errors += errors;
            return this;
        }

        int getValid() {
            return valid;
        }

        public void setValid(int valid) {
            this.valid = valid;
        }

        int getInvalid() {
            return invalid;
        }

        public void setInvalid(int invalid) {
            this.invalid = invalid;
        }

        int getPending() {
            return pending;
        }

        public void setPending(int pending) {
            this.pending = pending;
        }

        int getErrors() {
            return errors;
        }

        public void setErrors(int errors) {
            this.errors = errors;
        }
    }
}
