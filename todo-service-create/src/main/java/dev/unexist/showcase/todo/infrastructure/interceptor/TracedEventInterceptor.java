/**
 * @package Showcase
 * @file
 * @copyright 2021 Christoph Kappel <christoph@unexist.dev>
 * @version $Id\$
 *
 *         This program can be distributed under the terms of the Apache License v2.0.
 *         See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.infrastructure.interceptor;

import dev.unexist.showcase.todo.infrastructure.tracing.TraceService;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.TracingKafkaUtils;
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord;
import io.smallrye.reactive.messaging.kafka.OutgoingKafkaRecord;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@TracedEventListener
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
public class TracedEventInterceptor {
    private static final String PREFIX_EVENT_LISTENER = "EventListener_";

    private static final Logger LOGGER = LoggerFactory.getLogger(TracedEventInterceptor.class);

    @Inject
    Tracer tracer;

    @AroundInvoke
    public Object wrap(InvocationContext context) throws Exception {
        Object result = null;
        Object parameter = context.getParameters()[0];

        LOGGER.info("instance={}", parameter.getClass());

        if (parameter instanceof IncomingKafkaRecord) {
            IncomingKafkaRecord<?, ?> record = (IncomingKafkaRecord<?, ?>) parameter;
            Headers headers = record.getHeaders();
            SpanContext parentContext = TracingKafkaUtils
                    .extractSpanContext(headers, tracer);

            Span span = tracer
                    .buildSpan(PREFIX_EVENT_LISTENER + context.getMethod().getName())
                    .asChildOf(parentContext)
                    .start();

            try (Scope scope = tracer.activateSpan(span)) {
                result = context.proceed();

                if (result instanceof OutgoingKafkaRecord) {
                    OutgoingKafkaRecord<?, ?> outgoingKafkaRecord =
                            (OutgoingKafkaRecord<?, ?>) result;

                    // Inject the default header ("uber-trace-id") that is used for extracting
                    // the span context on consumer side.
                    result = outgoingKafkaRecord
                            .withHeader(TraceService.JAEGER_PROPAGATION_HEADER,
                                    span.context().toString());
                }
            }
        } else {
            result = context.proceed();
        }


        return result;
    }
}