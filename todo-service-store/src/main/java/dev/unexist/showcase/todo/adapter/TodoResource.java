/**
 * @package Showcase-Logging-Tracing-Quarkus
 *
 * @file Todo resource
 * @copyright 2021-2022 Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.adapter;

import com.tersesystems.echopraxia.Logger;
import com.tersesystems.echopraxia.LoggerFactory;
import dev.unexist.showcase.todo.domain.todo.Todo;
import dev.unexist.showcase.todo.domain.todo.TodoService;
import io.opentelemetry.api.trace.Span;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("/todo")
public class TodoResource {
    private static final Logger<Todo.FieldBuilder> LOGGER = LoggerFactory.getLogger(TodoResource.class)
            .withFieldBuilder(Todo.FieldBuilder.class);

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
