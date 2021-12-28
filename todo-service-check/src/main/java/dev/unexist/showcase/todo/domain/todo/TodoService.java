/**
 * @package Quarkus-Logging-Tracing-Quarkus
 *
 * @file Todo service and domain service
 * @copyright 2020-2021 Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.domain.todo;

import io.opentelemetry.extension.annotations.WithSpan;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;

import static org.awaitility.Awaitility.await;

@ApplicationScoped
public class TodoService {

    /**
     * Check whether the due date is after the start date of the given {@link Todo}
     *
     * @param  base  A {@link TodoBase} entry
     *
     * @return Either {@code true} if the due date is after the start date; {@code false}
     **/

    @WithSpan
    public boolean check(TodoBase base) {
        await().between(Duration.ofSeconds(1), Duration.ofSeconds(5));

        return base.getDueDate().getDue().isAfter(base.getDueDate().getStart());
    }
}
