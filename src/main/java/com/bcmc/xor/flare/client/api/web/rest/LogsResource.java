package com.bcmc.xor.flare.client.api.web.rest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

import com.bcmc.xor.flare.client.api.domain.auth.User;
import com.bcmc.xor.flare.client.api.web.rest.vm.LoggerVM;
import com.bcmc.xor.flare.client.error.NameIsNullException;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for view and managing Log Level at runtime.
 */
@SuppressWarnings("unused")
@RestController
@RequestMapping("/management")
public class LogsResource {
	
	private static final Logger log = LoggerFactory.getLogger(AuditResource.class);

    @GetMapping("/logs")
    @Timed
    public List<LoggerVM> getList() {
    	log.debug("REST request to get all log levels");
    	
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        return context.getLoggerList()
            .stream()
            .map(LoggerVM::new)
            .collect(Collectors.toList());
    }

    @PutMapping("/logs")
    @Timed
    public ResponseEntity<Object> changeLevel(@Valid @RequestBody LoggerVM jsonLogger) {	
    	log.debug("REST request to update log levels");
    	
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();        
        context.getLogger(jsonLogger.getName()).setLevel(Level.valueOf(jsonLogger.getLevel()));  
        return new ResponseEntity<>("Log level successfully changed for " + jsonLogger.getName(), HttpStatus.OK);
    }
}
