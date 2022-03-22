/**
 * @package Showcase-Logging-Tracing-Quarkus
 *
 * @file Todo service and domain service
 * @copyright 2021-2022 Christoph Kappel <christoph@unexist.dev>
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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;

@ApplicationScoped
public class TodoService {
    private static final Logger<Todo.FieldBuilder> LOGGER = LoggerFactory.getLogger(TodoService.class)
            .withFieldBuilder(Todo.FieldBuilder.class);

    private static final List<String> BADWORDS = List.of("badword");

    @Inject
    TodoRepository todoRepository;

    /**
     * Check whether {@link Todo} contains any badwords
     *
     * @param  todo  A {@link Todo} entry
     *
     * @return Either {@code true} if a badword is found; otherwise {@code false}
     **/

    @WithSpan("Verify todo")
    public boolean verify(Todo todo) {
        await().between(Duration.ofSeconds(1), Duration.ofSeconds(10));

        boolean invalid = Stream.of(todo.getTitle(), todo.getDescription())
                        .anyMatch(part -> BADWORDS.stream()
                                .anyMatch(part::contains));

        LOGGER.info("Verified todo: {}, {}",
                fb -> List.of(fb.todo("todo", todo),
                        fb.bool("invalid", invalid)));

        Span.current()
                .addEvent("Verified todo", Attributes.of(
                        AttributeKey.stringKey("id"), todo.getId(),
                        AttributeKey.stringKey("invalid"), String.valueOf(invalid)))
                .setStatus(invalid ? StatusCode.ERROR : StatusCode.OK);

        todo.setVerified(true);

        return invalid;
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
                    fb -> fb.onlyTodo("todo", todo));

            Span.current()
                    .addEvent("Updated todo", Attributes.of(
                            AttributeKey.stringKey("id"), todo.getId()))
                    .setStatus(StatusCode.OK);

            ret = true;
        } else {
            LOGGER.error("Cannot update todo: {}",
                    fb -> List.of(fb.todo("todo", todo)));

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
