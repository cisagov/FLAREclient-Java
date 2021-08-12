package gov.dhs.cisa.ctm.flare.client.api.web.rest;

import com.codahale.metrics.annotation.Timed;

import gov.dhs.cisa.ctm.flare.client.api.domain.parameters.UploadedFile;
import gov.dhs.cisa.ctm.flare.client.taxii.Validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by allenpreville on 6/9/17.
 */
@RestController
@RequestMapping("/api/servers/{serverLabel}/collections/{collectionId}/validate")
public class ValidationResource {
    private static final Logger log = LoggerFactory.getLogger(ValidationResource.class);

    @PostMapping
    @Timed
    public ResponseEntity<Map<Integer, List<String>>> validateStixFile(@RequestBody Map<Integer, UploadedFile> listOfContent) {
        log.debug("Validating {} files", listOfContent.size());

        if (listOfContent.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Map<Integer, List<String>> validationResults = new HashMap<>();
        //For each file
        for (Map.Entry<Integer, UploadedFile> content: listOfContent.entrySet()) {
            validationResults.put(content.getKey(), null);
            try {
                //validate the stix string
                Map.Entry<String, List<String>> errors = Validation.getInstance().validateAndReturnErrors(content.getValue().getContent());
                //if errors were found, add them to our list of results
                if (!errors.getValue().isEmpty()) {
                    log.warn("Validation errors for '{}' ('{}'): {}", errors.getKey(), content.getValue().getFilename(), String.join(", ", errors.getValue()));
                }

                validationResults.put(content.getKey(), errors.getValue());

            } catch (Exception e) {
                //If a file failed to be converted to a string, add that to the result list
                validationResults.put(content.getKey(), Collections.singletonList("Failed validation due to " + e.getMessage()));
            }
        }

        return ResponseEntity.ok().body(validationResults);
    }
}
