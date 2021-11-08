/**
 * @package Quarkus-Observability-Showcase
 *
 * @file Todo resource
 * @copyright 2020-2021 Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0. See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.unexist.showcase.todo.domain.todo.TodoBase;
import io.netty.util.internal.StringUtil;
import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.Record;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.List;
import java.util.Random;

@ApplicationScoped
public class TodoGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(TodoGenerator.class);
    private static final int DELAY_IN_SECONDS = 300;

    private final Random random = new Random();

    private final ObjectMapper mapper = new ObjectMapper();

    private List<TodoBase> todos = List.of(
            new TodoBase("First", "Bla"),
            new TodoBase("Second", "Bla-bla"),
            new TodoBase("Third", "Bla-bla-bla"),
            new TodoBase("Fourth", "Bla-bla-bla-bla"),
            new TodoBase("Fifth", "Bla-bla-bla-bla-bla"),
            new TodoBase("Sixth", "Bla-bla-bla-bla-bla-bla"));

    @Outgoing("todo-generator")
    public Multi<Record<Integer, String>> generateTodos() {
        return Multi.createFrom()
                .ticks()
                .every(Duration.ofSeconds(DELAY_IN_SECONDS))
                .onOverflow().drop()
                .map(tick -> {
                    int idx = random.nextInt(todos.size());
                    TodoBase base = todos.get(idx);

                    String json = StringUtil.EMPTY_STRING;

                    try {
                        json = this.mapper.writeValueAsString(base);
                    } catch (JsonProcessingException e) {
                        LOGGER.error("Error converting todo", e);
                    }

                    LOGGER.info("Created todo {} with payload {}", idx, json);

                    return Record.of(idx, json);
                });
    }
}
