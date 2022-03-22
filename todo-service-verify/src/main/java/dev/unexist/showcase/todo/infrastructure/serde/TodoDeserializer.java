/**
 * @package Showcase-Logging-Tracing-Quarkus
 *
 * @file Todo deserializer
 * @copyright 2022 Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.infrastructure.serde;

import dev.unexist.showcase.todo.domain.todo.Todo;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class TodoDeserializer extends ObjectMapperDeserializer<Todo> {

    /**
     * Construct deserializer for {@link Todo}
     **/

    public TodoDeserializer() {
        super(Todo.class);
    }
}
