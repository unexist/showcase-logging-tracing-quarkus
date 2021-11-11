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

import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.contrib.interceptors.OpenTracingInterceptor;
import io.opentracing.contrib.kafka.TracingKafkaUtils;
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(TracedEventInterceptor.class);

    @Inject
    Tracer tracer;

    @AroundInvoke
    public Object wrap(InvocationContext context) throws Exception {
        for (int i = 0; i < context.getParameters().length; i++) {
            Object parameter = context.getParameters()[i];

            LOGGER.info("instance={}", parameter.getClass());

            if (parameter instanceof IncomingKafkaRecord) {
                IncomingKafkaRecord<?, ?> record = (IncomingKafkaRecord<?, ?>) parameter;
                Headers headers = record.getHeaders();
                SpanContext spanContext = TracingKafkaUtils
                        .extractSpanContext(headers, tracer);

                context.getContextData().put(OpenTracingInterceptor.SPAN_CONTEXT, spanContext);
            }
        }

        return context.proceed();
    }
}
