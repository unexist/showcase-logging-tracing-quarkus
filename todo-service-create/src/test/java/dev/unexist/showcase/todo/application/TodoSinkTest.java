/**
 * @package Quarkus-Logging-Tracing-Quarkus
 *
 * @file Stupid integration test
 * @copyright 2020-2021 Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.application;

import dev.unexist.showcase.todo.domain.todo.TodoService;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.providers.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.providers.connectors.InMemorySource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class TodoSinkTest {
    public static final String TODO_AS_JSON = "{\"description\": \"string\", \"done\": true, "
        + "\"dueDate\": { \"due\": \"2021-05-07\", \"start\": \"2021-05-07\" }, \"title\": \"string\" }";

    @Inject @Any
    InMemoryConnector connector;

    @Inject
    TodoService todoService;

    @BeforeAll
    public static void switchChannels() {
        InMemoryConnector.switchOutgoingChannelsToInMemory("todo-generator");
        InMemoryConnector.switchIncomingChannelsToInMemory("todo-sink");
    }

    @AfterAll
    public static void revertChannels() {
        InMemoryConnector.clear();
    }

    @Test
    void testSink() {
        InMemorySource<String> todoSource = this.connector.source("todo-sink");

        todoSource.send(TODO_AS_JSON);

        assertThat(this.todoService.getAll().size()).isEqualTo(1);
    }
}
