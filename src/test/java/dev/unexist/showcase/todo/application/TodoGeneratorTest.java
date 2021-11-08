package dev.unexist.showcase.todo.application;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySink;
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
