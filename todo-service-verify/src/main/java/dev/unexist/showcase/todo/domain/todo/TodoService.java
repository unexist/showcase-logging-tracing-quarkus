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
import java.time.Duration;
import java.util.Optional;

import static org.awaitility.Awaitility.await;

@ApplicationScoped
public class TodoService {

    @Inject
    TodoRepository todoRepository;

    /**
     * Check whether the due date is after the start date of the given {@link Todo}
     *
     * @param  todo  A {@link Todo} entry
     *
     * @return Either {@code true} if the due date is after the start date; {@code false}
     **/

    @WithSpan("Check todo")
    public boolean verify(Todo todo) {
        await().between(Duration.ofSeconds(1), Duration.ofSeconds(10));

        boolean result = todo.getDueDate().getDue().isAfter(todo.getDueDate().getStart());

        Span.current()
                .updateName("Checked todo")
                .addEvent("Checked todo", Attributes.of(
                        AttributeKey.stringKey("valid"), String.valueOf(result)));

        return result;
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
            Span.current()
                    .addEvent("Updated todo", Attributes.of(
                            AttributeKey.stringKey("id"), todo.getId()))
                    .setStatus(StatusCode.OK);
        } else {
            Span.current()
                    .setStatus(StatusCode.ERROR, "Todo not found");
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
