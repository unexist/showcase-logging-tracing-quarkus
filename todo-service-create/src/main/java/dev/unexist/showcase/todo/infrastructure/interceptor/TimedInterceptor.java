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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.util.concurrent.TimeUnit;

@Timed
@Priority(10)
@Interceptor
public class TimedInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimedInterceptor.class);

    @AroundInvoke
    public Object log(InvocationContext context) throws Exception {
        long start = System.nanoTime();

        Object result = context.proceed();

        long end = System.nanoTime() - start;

        LOGGER.info("Execution of {}.{} took {}ms", context.getTarget().getClass().getSimpleName(),
                context.getMethod().getName(), TimeUnit.MILLISECONDS.toMillis(end));

        return result;
    }
}
