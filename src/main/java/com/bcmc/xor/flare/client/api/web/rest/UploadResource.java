package com.bcmc.xor.flare.client.api.web.rest;

import com.bcmc.xor.flare.client.api.domain.parameters.UploadedFile;
import com.bcmc.xor.flare.client.api.service.CollectionService;
import com.bcmc.xor.flare.client.api.service.ServerService;
import com.bcmc.xor.flare.client.api.service.UploadService;
import com.bcmc.xor.flare.client.taxii.TaxiiAssociation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/servers/{serverLabel}/collections/{collectionId}/upload")
class UploadResource {

    private static final Logger log = LoggerFactory.getLogger(UploadResource.class);


    private final ServerService serverService;
    private final CollectionService collectionService;
    private final UploadService uploadService;

    public UploadResource(ServerService serverService, CollectionService collectionService, UploadService uploadService) {
        this.serverService = serverService;
        this.uploadService = uploadService;
        this.collectionService = collectionService;
    }

    @PostMapping
    public ResponseEntity<String> publish(@RequestParam("file") MultipartFile file, @RequestParam("filename") String filename, @PathVariable String serverLabel, @PathVariable String collectionId) throws IOException {
        log.debug("REST request to publish file {} with serverLabel {} and collectionId {}", filename, serverLabel,collectionId);

        Map<String, UploadedFile> fileMap = new HashMap<>();
        ByteArrayInputStream stream = new ByteArrayInputStream(file.getBytes());
        String content = IOUtils.toString(stream);
        UploadedFile  uploadedFile = new UploadedFile();
        uploadedFile.setFilename(filename);
        uploadedFile.setContent(content);
        uploadedFile.setHash(1);
        fileMap.put(filename,uploadedFile);
        TaxiiAssociation association = TaxiiAssociation.from(serverLabel, collectionId, serverService, collectionService);
        String feedback = uploadService.publish(association, fileMap);
        if (StringUtils.containsIgnoreCase(feedback,"FAIL")) {
            return new ResponseEntity<>(feedback, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(feedback, HttpStatus.OK);
    }
}
