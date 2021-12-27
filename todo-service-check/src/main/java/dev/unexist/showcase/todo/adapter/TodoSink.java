/**
 * @package Quarkus-Logging-Tracing-Quarkus
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
import io.opentelemetry.context.Context;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
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

    /**
     * Receive {@link TodoBase} from Kafka
     *
     * @param  record  A {@link IncomingKafkaRecord} to handle
     **/

    @Incoming("todo-created")
    @Outgoing("todo-checked")
    public Message<String> consumeTodos(IncomingKafkaRecord<Integer, String> record) {
        Message<String> outMessage = null;

        LOGGER.info("Received message from todo-created");

        try {
            if (this.todoService.check(this.mapper.readValue(record.getPayload(), TodoBase.class))) {
                outMessage = Message.of(record.getPayload())
                        .withMetadata(Metadata.of(TracingMetadata.withPrevious(Context.current())));
            }

            LOGGER.info("Received todo with payload {}", record.getPayload());
        } catch (JsonProcessingException e) {
            LOGGER.error("Error reading JSON", e);
        }

        return outMessage;
    }
}


