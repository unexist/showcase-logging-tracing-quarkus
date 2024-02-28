/**
 * @package Showcase-Logging-Tracing-Quarkus
 *
 * @file Todo service and domain service
 * @copyright 2021-present Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.domain.todo;

import com.tersesystems.echopraxia.Logger;
import com.tersesystems.echopraxia.LoggerFactory;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.extension.annotations.WithSpan;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.awaitility.Awaitility.await;

@ApplicationScoped
public class TodoService {
    private static final Logger<Todo.FieldBuilder> LOGGER =
            LoggerFactory.getLogger(TodoService.class, Todo.FieldBuilder.INSTANCE);

    @Inject
    TodoRepository todoRepository;

    /**
     * Create new {@link Todo} entry and store it in repository
     *
     * @param  base  A {@link TodoBase} entry
     *
     * @return Either id of the entry on success; otherwise {@code -1}
     **/

    @WithSpan("Create todo")
    public Optional<Todo> create(TodoBase base) {
        Todo todo = new Todo(base);

        todo.setId(UUID.randomUUID().toString());

        await().between(Duration.ofSeconds(1), Duration.ofSeconds(10));

        LOGGER.info("Added id to todo: {}",
                fb -> fb.todo("todo", todo));

        Span.current()
                .addEvent("Added id to todo", Attributes.of(
                        AttributeKey.stringKey("id"), todo.getId()))
                .setStatus(StatusCode.OK);

        return Optional.of(todo);
    }

    /**
     * Update {@link Todo} at with given id
     *
     * @param  todo  A {@link Todo} to update
     *
     * @return Either {@code true} on success; otherwise {@code false}
     **/

    @WithSpan("Update todo")
    public boolean update(Todo todo) {
        boolean ret = false;

        if (this.todoRepository.update(todo)) {
            LOGGER.info("Updated todo: {}",
                    fb -> fb.todo("todo", todo));

            Span.current()
                    .addEvent("Updated todo", Attributes.of(
                            AttributeKey.stringKey("id"), todo.getId()))
                    .setStatus(StatusCode.OK);

            ret = true;
        } else {
            LOGGER.error("Cannot update todo: {}",
                    fb -> fb.todo("todo", todo));

            Span.current()
                    .setStatus(StatusCode.ERROR, "Cannot update todo");
        }

        return ret;
    }

    /**
     * Find {@link Todo} by given id
     *
     * @param  id  Id to look for
     *
     * @return A {@link Optional} of the entry
     **/

    public Optional<Todo> findById(String id) {
        return this.todoRepository.findById(id);
    }
}
