/**
 * @package Showcase-Logging-Tracing-Quarkus

 * @file Log time interceptor
 * @copyright 2022-present Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.infrastructure.interceptor;

import io.opentelemetry.api.trace.Span;
import org.slf4j.MDC;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Traced
@Priority(10)
@Interceptor
public class TracedInterceptor {

    @AroundInvoke
    public Object trace(InvocationContext context) throws Exception {
        Object result = null;

        try (MDC.MDCCloseable closable = MDC.putCloseable("trace_id",
                Span.current().getSpanContext().getTraceId())) {
            result = context.proceed();
        }

        return result;
    }
}