/**
 * @package Showcase-Logging-Tracing-Quarkus
 *
 * @file Todo source
 * @copyright 2021-2022 Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.adapter;

import com.tersesystems.echopraxia.Logger;
import com.tersesystems.echopraxia.LoggerFactory;
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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class TodoSource {
    private static final Logger<Todo.FieldBuilder> LOGGER = LoggerFactory.getLogger(TodoSource.class)
            .withFieldBuilder(Todo.FieldBuilder.class);

    @Inject
    @Channel("todo-verified")
    Emitter<Todo> emitter;

    /**
     * Send {@link Todo} to topic
     *
     * @param  todo  A {@link Todo} to convert and send
     **/

    @WithSpan("Sent message to todo-verified")
    public void send(Todo todo) {
        LOGGER.info("Sent message to todo-verified: {}",
                fb -> List.of(fb.todo("todo", todo)));

        Message<Todo> outMessage = Message.of(todo)
                .withMetadata(Metadata.of(
                        TracingMetadata.withPrevious(Context.current())
                                .withSpan(Span.current())));

        this.emitter.send(outMessage);

        Span.current().setStatus(StatusCode.OK);
    }
}
