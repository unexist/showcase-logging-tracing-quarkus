/**
 * @package Showcase-Logging-Tracing-Quarkus
 *
 * @file Todo resource
 * @copyright 2021-present Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.adapter;

import com.tersesystems.echopraxia.Logger;
import com.tersesystems.echopraxia.LoggerFactory;
import dev.unexist.showcase.todo.domain.todo.Todo;
import dev.unexist.showcase.todo.domain.todo.TodoBase;
import dev.unexist.showcase.todo.domain.todo.TodoService;
import dev.unexist.showcase.todo.infrastructure.interceptor.Timed;
import dev.unexist.showcase.todo.infrastructure.interceptor.Traced;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Optional;

@Path("/todo")
public class TodoResource {
    private static final Logger<Todo.FieldBuilder> LOGGER =
            LoggerFactory.getLogger(TodoResource.class, Todo.FieldBuilder.INSTANCE);

    @Inject
    TodoService todoService;

    @Inject
    TodoSource todoSource;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create new todo")
    @Tag(name = "Todo")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Todo created"),
            @APIResponse(responseCode = "406", description = "Bad data"),
            @APIResponse(responseCode = "500", description = "Server error")
    })
    @Traced
    @Timed
    public Response create(TodoBase todoBase, @Context UriInfo uriInfo) {
        Response.ResponseBuilder response;

        LOGGER.info("Received post request");

        Span.current()
                .updateName("Received post request");

        Optional<Todo> todo = this.todoService.create(todoBase);

        if (todo.isPresent()) {
            Span.current()
                    .setStatus(StatusCode.OK, todo.get().getId());

            this.todoSource.send(todo.get());

            LOGGER.info("Created todo: {}",
                    fb -> fb.todo("todo", todo.get()));

            URI uri = uriInfo.getAbsolutePathBuilder()
                    .path(todo.get().getId())
                    .build();

            response = Response.created(uri);
        } else {
            LOGGER.error("Error creating todo");

            Span.current()
                    .setStatus(StatusCode.ERROR, "Error creating todo");

            response = Response.status(Response.Status.NOT_ACCEPTABLE);
        }

        return response.build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get todo by id")
    @Tag(name = "Todo")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Todo found", content =
                @Content(schema = @Schema(implementation = Todo.class))),
            @APIResponse(responseCode = "404", description = "Todo not found"),
            @APIResponse(responseCode = "500", description = "Server error")
    })
    public Response findById(@PathParam("id") String id) {
        Optional<Todo> result = this.todoService.findById(id);

        Response.ResponseBuilder response;

        LOGGER.info("Received get request");

        Span.current()
                .updateName("Received get request");

        if (result.isPresent()) {
            response = Response.ok(Entity.json(result.get()));
        } else {
            response = Response.status(Response.Status.NOT_FOUND);
        }

        return response.build();
    }
}
