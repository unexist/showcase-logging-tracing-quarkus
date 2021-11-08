/**
 * @package Quarkus-Observability-Showcase
 *
 * @file Todo sink
 * @copyright 2020-2021 Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0. See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.unexist.showcase.todo.domain.todo.TodoBase;
import dev.unexist.showcase.todo.domain.todo.TodoService;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TodoSink {
    private static final Logger LOGGER = LoggerFactory.getLogger(TodoSink.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    TodoService todoService;

    @Incoming("todo-sink")
    public void consumeTodos(String json) {
        TodoBase todo = null;

        try {
            todo = this.mapper.readValue(json, TodoBase.class);

            LOGGER.info("Received todo with payload {}", json);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error reading JSON", e);
        }

        todoService.create(todo);
    }
}


