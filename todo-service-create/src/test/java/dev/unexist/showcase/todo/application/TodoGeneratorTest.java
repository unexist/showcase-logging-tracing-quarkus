/**
 * @package Quarkus-Logging-Tracing-Quarkus
 *
 * @file Stupid integration test
 * @copyright 2021-2022 Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.application;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.providers.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.providers.connectors.InMemorySink;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class TodoGeneratorTest {
    @Inject @Any
    InMemoryConnector connector;

    @BeforeAll
    public static void switchChannels() {
        InMemoryConnector.switchOutgoingChannelsToInMemory("todo-generator");
    }

    @AfterAll
    public static void revertChannels() {
        InMemoryConnector.clear();
    }

    @Test
    void testGenerator() {
        InMemorySink<String> todoSink = this.connector.sink("todo-generator");

        assertThat(todoSink.received().size()).isEqualTo(1);
    }
}
