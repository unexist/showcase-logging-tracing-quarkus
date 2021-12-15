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

import io.opentelemetry.context.Context;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord;
import io.smallrye.reactive.messaging.kafka.OutgoingKafkaRecord;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@TracedEventListener
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
public class TracedEventInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TracedEventInterceptor.class);

    @AroundInvoke
    public Object wrap(InvocationContext context) throws Exception {
        Object result = null;
        Object parameter = context.getParameters()[0];

        LOGGER.info("instance={}", parameter.getClass());

        if (parameter instanceof IncomingKafkaRecord) {
            IncomingKafkaRecord<?, ?> record = (IncomingKafkaRecord<?, ?>) parameter;

            result = context.proceed();

            if (result instanceof OutgoingKafkaRecord) {
                OutgoingKafkaRecord<?, ?> outgoingKafkaRecord =
                        (OutgoingKafkaRecord<?, ?>) result;

                result = outgoingKafkaRecord
                        .withMetadata(Metadata.of(TracingMetadata.withPrevious(Context.current())));
            }
        } else {
            result = context.proceed();
        }

        return result;
    }
}