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
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class TodoSink {
    private static final Logger LOGGER = LoggerFactory.getLogger(TodoSink.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @ConfigProperty(name = "quarkus.application.name")
    String appName;

    @Inject
    TodoService todoService;

    @Incoming("todo-stored")
    public CompletionStage<Void> consumeStored(IncomingKafkaRecord<String, String> record) {
        LOGGER.info("Received message from todo-stored: payload={}", record.getPayload());

        Optional<TracingMetadata> metadata = TracingMetadata.fromMessage(record);

        if (metadata.isPresent()) {
            try (Scope ignored = metadata.get().getCurrentContext().makeCurrent()) {
                Span span = GlobalOpenTelemetry.getTracer(appName)
                        .spanBuilder("Received message from todo-stored").startSpan();

                try {
                    Todo todo = this.mapper.readValue(record.getPayload(), Todo.class);

                    if (this.todoService.update(todo)) {
                        span.addEvent("Updated todo", Attributes.of(
                                AttributeKey.stringKey("id"), todo.getId()));
                    }
                } catch (JsonProcessingException e) {
                    LOGGER.error("Error handling JSON", e);

                    span.recordException(e)
                            .setStatus(StatusCode.ERROR, "Error handling JSON");
                }

                span.end();
            }
        }

        return record.ack();
    }
}


