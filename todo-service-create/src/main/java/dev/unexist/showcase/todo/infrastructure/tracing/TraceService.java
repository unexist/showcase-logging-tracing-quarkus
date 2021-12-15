/**
 * @package Showcase
 * @file Tracing util
 * @copyright 2021 Christoph Kappel <christoph@unexist.dev>
 * @version $Id\$
 *
 *         This program can be distributed under the terms of the Apache License v2.0.
 *         See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.infrastructure.tracing;

import io.opentelemetry.context.Context;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.kafka.OutgoingKafkaRecord;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TraceService {
    public <K, P> OutgoingKafkaRecord<K, P> createTracedRecord(K key, P payload) {
        OutgoingKafkaRecordMetadata.OutgoingKafkaRecordMetadataBuilder<K> kafkaMetadata =
                OutgoingKafkaRecordMetadata.<K>builder().withKey(key);

        Message<P> outMessage = Message.of(payload)
                .withMetadata(Metadata.of(kafkaMetadata))
                .withMetadata(Metadata.of(TracingMetadata.withPrevious(Context.current())));

        return OutgoingKafkaRecord.from(outMessage);
    }
}
