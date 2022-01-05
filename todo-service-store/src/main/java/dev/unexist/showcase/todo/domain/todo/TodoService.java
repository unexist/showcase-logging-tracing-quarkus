/**
 * @package Quarkus-Logging-Tracing-Quarkus
 *
 * @file Todo service and domain service
 * @copyright 2021-2022 Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.domain.todo;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.extension.annotations.WithSpan;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

@ApplicationScoped
public class TodoService {

    @Inject
    TodoRepository todoRepository;

    /**
     * Store {@link Todo}
     *
     * @param  todo  A {@link Todo} to store
     *
     * @return Either {@code true} on success; otherwise {@code false}
     **/

    @WithSpan("Store todo")
    public boolean store(Todo todo) {
        boolean ret = false;

        if (this.todoRepository.add(todo)) {
            Span.current()
                    .addEvent("Stored todo", Attributes.of(
                            AttributeKey.stringKey("id"), todo.getId()))
                    .setStatus(StatusCode.OK);
        } else {
            Span.current()
                    .setStatus(StatusCode.ERROR);
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
