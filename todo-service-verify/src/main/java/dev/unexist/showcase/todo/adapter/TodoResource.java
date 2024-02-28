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

import dev.unexist.showcase.todo.domain.todo.Todo;
import dev.unexist.showcase.todo.domain.todo.TodoService;
import io.opentelemetry.api.trace.Span;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Optional;

@Path("/todo")
public class TodoResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(TodoResource.class);

    @Inject
    TodoService todoService;

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
