/**
 * @package Quarkus-Logging-Tracing-Quarkus
 *
 * @file Todo sink
 * @copyright 2021-2022 Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0. See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.unexist.showcase.todo.domain.todo.Todo;
import dev.unexist.showcase.todo.domain.todo.TodoService;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class TodoSink {
    private static final Logger LOGGER = LoggerFactory.getLogger(TodoSink.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    TodoService todoService;

    @Incoming("todo-stored")
    public CompletionStage<Void> consume(IncomingKafkaRecord<String, String> record) {
        LOGGER.info("Received message from todo-stored: payload={}", record.getPayload());

        TracingMetadata.fromMessage(record)
                .ifPresent(meta -> meta.getCurrentContext().makeCurrent());

        Span.current()
                .updateName("Received message from todo-stored");

        try {
            Todo todo = this.mapper.readValue(record.getPayload(), Todo.class);

            if (this.todoService.update(todo)) {
                Span.current()
                        .addEvent("Updated todo", Attributes.of(
                                AttributeKey.stringKey("id"), todo.getId()));
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("Error handling JSON", e);

            Span.current()
                    .recordException(e)
                    .setStatus(StatusCode.ERROR, "Error handling JSON");
        }

        Span.current().end();

        return record.ack();
    }
}


