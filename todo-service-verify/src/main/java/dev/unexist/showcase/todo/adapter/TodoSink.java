/**
 * @package Showcase-Logging-Tracing-Quarkus
 *
 * @file Todo sink
 * @copyright 2021-2022 Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0. See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.adapter;

import com.tersesystems.echopraxia.Logger;
import com.tersesystems.echopraxia.LoggerFactory;
import dev.unexist.showcase.todo.domain.todo.Todo;
import dev.unexist.showcase.todo.domain.todo.TodoService;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class TodoSink {
    private static final Logger<Todo.FieldBuilder> LOGGER = LoggerFactory.getLogger(TodoSink.class)
            .withFieldBuilder(Todo.FieldBuilder.class);

    @ConfigProperty(name = "quarkus.application.name")
    String appName;

    @Inject
    TodoService todoService;

    @Inject
    TodoSource todoSource;

    @Incoming("todo-created")
    public CompletionStage<Void> consumeCreated(IncomingKafkaRecord<String, Todo> record) {
        LOGGER.info("Received message from todo-created: {}",
                fb -> fb.onlyTodo("payload", record.getPayload()));

        Optional<TracingMetadata> metadata = TracingMetadata.fromMessage(record);

        if (metadata.isPresent()) {
            try (Scope ignored = metadata.get().getCurrentContext().makeCurrent()) {
                Span span = GlobalOpenTelemetry.getTracer(appName)
                        .spanBuilder("Received message from todo-created").startSpan();

                this.todoService.verify(record.getPayload());

                span.addEvent("Verified todo", Attributes.of(
                        AttributeKey.stringKey("id"), record.getPayload().getId()));

                this.todoSource.send(record.getPayload());

                span.end();
            }
        }

        return record.ack();
    }

    @Incoming("todo-stored")
    public CompletionStage<Void> consumeStored(IncomingKafkaRecord<String, Todo> record) {
        LOGGER.info("Received message from todo-stored: {}",
                fb -> fb.onlyTodo("payload", record.getPayload()));

        Optional<TracingMetadata> metadata = TracingMetadata.fromMessage(record);

        if (metadata.isPresent()) {
            try (Scope ignored = metadata.get().getCurrentContext().makeCurrent()) {
                Span span = GlobalOpenTelemetry.getTracer(appName)
                        .spanBuilder("Received message from todo-stored").startSpan();

                if (this.todoService.update(record.getPayload())) {
                    span.addEvent("Updated todo", Attributes.of(
                            AttributeKey.stringKey("id"), record.getPayload().getId()));
                }

                span.end();
            }
        }

        return record.ack();
    }
}


