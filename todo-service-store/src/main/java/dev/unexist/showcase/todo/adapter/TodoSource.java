/**
 * @package Quarkus-Logging-Tracing-Quarkus
 *
 * @file Todo source
 * @copyright 2021-2022 Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.unexist.showcase.todo.domain.todo.Todo;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.extension.annotations.WithSpan;
import io.smallrye.reactive.messaging.TracingMetadata;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TodoSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(TodoSource.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    @Channel("todo-stored")
    Emitter<String> emitter;

    /**
     * Send {@link Todo} to topic
     *
     * @param  todo  A {@link Todo} to convert and send
     **/

    @WithSpan("Sent message to todo-created")
    public void send(Todo todo) {
        LOGGER.info("Sent message to todo-stored");

        try {
            String json = this.mapper.writeValueAsString(todo);

            Message<String> outMessage = Message.of(json)
                    .withMetadata(Metadata.of(
                            TracingMetadata.withPrevious(Context.current())
                                    .withSpan(Span.current())));

            this.emitter.send(outMessage);

            Span.current().setStatus(StatusCode.OK);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error handling JSON", e);

            Span.current()
                    .recordException(e)
                    .setStatus(StatusCode.ERROR, "Error handling JSON");
        }
    }
}
