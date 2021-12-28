/**
 * @package Quarkus-Logging-Tracing-Quarkus
 *
 * @file Todo source
 * @copyright 2020-2021 Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.adapter;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
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

    @Inject
    @Channel("todo-created")
    Emitter<String> emitter;

    /**
     * Send json string to topic
     *
     * @param  json  JSON string to send
     **/

    public void send(String json) {
        LOGGER.info("Sent message to todo-created");

        Message<String> outMessage = Message.of(json)
                .withMetadata(Metadata.of(
                        TracingMetadata.withPrevious(Context.current())
                                .withSpan(Span.current())));

        this.emitter.send(outMessage);
    }
}
