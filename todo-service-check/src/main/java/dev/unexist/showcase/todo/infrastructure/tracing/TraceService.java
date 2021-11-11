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

import io.jaegertracing.internal.JaegerSpan;
import io.jaegertracing.internal.JaegerSpanContext;
import io.opentracing.Tracer;
import io.smallrye.reactive.messaging.kafka.OutgoingKafkaRecord;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TraceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TraceService.class);

    private static final String JAEGER_PROPAGATION_HEADER = "uber-trace-id";

    @Inject
    Tracer tracer;

    public <K, P> OutgoingKafkaRecord<K, P> createTracedRecord(K key, P payload) {

        OutgoingKafkaRecordMetadata.OutgoingKafkaRecordMetadataBuilder<K> kafkaMetadata =
                OutgoingKafkaRecordMetadata.<K>builder().withKey(key);

        Message<P> outMessage = Message.of(payload).withMetadata(Metadata.of(kafkaMetadata));
        OutgoingKafkaRecord<K, P> outgoingRecord = OutgoingKafkaRecord
                .from(outMessage);

        JaegerSpanContext spanCtx = ((JaegerSpan)this.tracer.activeSpan()).context();

        // uber-trace-id format: {trace-id}:{span-id}:{parent-span-id}:{flags}
        // See https://www.jaegertracing.io/docs/1.28/client-libraries/#tracespan-identity
        String uberTraceId = String.format("%s:%s:%s:%s", spanCtx.getTraceId(),
                Long.toHexString(spanCtx.getSpanId()), Long.toHexString(spanCtx.getParentId()),
                Integer.toHexString(spanCtx.getFlags()));

        LOGGER.info("uber-trace-id={}", uberTraceId);

        return outgoingRecord.withHeader(JAEGER_PROPAGATION_HEADER, uberTraceId.getBytes());
    }
}
