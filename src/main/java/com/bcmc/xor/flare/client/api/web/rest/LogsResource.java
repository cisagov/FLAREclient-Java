package com.bcmc.xor.flare.client.api.web.rest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.bcmc.xor.flare.client.api.web.rest.vm.LoggerVM;
import com.bcmc.xor.flare.client.error.NameIsNullException;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        return context.getLoggerList()
            .stream()
            .map(LoggerVM::new)
            .collect(Collectors.toList());
    }

    @PutMapping("/logs")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Timed
    public void changeLevel(@Valid @RequestBody LoggerVM jsonLogger) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        if (jsonLogger.getName() == null) {
       	 log.error("Request body 'name' field is null.");
       	 throw new NameIsNullException();       	
       } else {
        context.getLogger(jsonLogger.getName()).setLevel(Level.valueOf(jsonLogger.getLevel()));
       }
    }
}
