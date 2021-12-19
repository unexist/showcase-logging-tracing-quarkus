/**
 * @package Quarkus-Logging-Tracing-Quarkus
 *
 * @file Todo resource
 * @copyright 2020-2021 Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0. See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.unexist.showcase.todo.domain.todo.TodoBase;
import dev.unexist.showcase.todo.infrastructure.tracing.TraceService;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TodoSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(TodoSource.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    TraceService traceService;

    @Inject
    @Channel("todo-checked")
    Emitter<String> emitter;

    /**
     * Send given {@link TodoBase} to Kafka
     *
     * @param  base  A {@link TodoBase} entry
     **/

    public void send(TodoBase base) {
        try {
            String json = this.mapper.writeValueAsString(base);

            this.emitter.send(this.traceService.createTracedRecord(1, json));

            LOGGER.info("Received todo with payload {}", json);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error converting todo", e);
        }
    }
}
