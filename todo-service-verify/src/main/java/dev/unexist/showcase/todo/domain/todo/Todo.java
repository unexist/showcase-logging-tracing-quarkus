/**
 * @package Showcase-Logging-Tracing-Quarkus
 *
 * @file Todo class and aggregate root
 * @copyright 2021-present Christoph Kappel <christoph@unexist.dev>
 * @version $Id$
 *
 * This program can be distributed under the terms of the Apache License v2.0.
 * See the file LICENSE for details.
 **/

package dev.unexist.showcase.todo.domain.todo;

import com.tersesystems.echopraxia.api.PresentationField;
import com.tersesystems.echopraxia.api.PresentationFieldBuilder;
import com.tersesystems.echopraxia.api.Value;

public class Todo extends TodoBase {
    private String id;

    /**
     * Constructor
     **/

    public Todo() {
    }

    /**
     * Constructor
     *
     * @param  base  Base entry
     **/

    public Todo(final TodoBase base) {
        this.update(base);
    }

    /**
     * Update values from base
     *
     * @param  base  Todo base class
     **/

    public void update(final TodoBase base) {
        this.setTitle(base.getTitle());
        this.setDescription(base.getDescription());
        this.setDone(base.getDone());
        this.setVerified(base.getVerified());

        if (null != base.getDueDate()) {
            this.setDueDate(base.getDueDate());
        }
    }

    /**
     * Get id of entry
     *
     * @return Id of the entry
     **/

    public String getId() {
        return id;
    }

    /**
     * Set id of entry
     *
     * @param  id  Id of the entry
     **/

    public void setId(String id) {
        this.id = id;
    }

    public interface FieldBuilder extends PresentationFieldBuilder {
        FieldBuilder INSTANCE = new FieldBuilder() {};

        /**
         * Field builder for {@link Todo}
         *
         * @param  name  Name of the log key
         * @param  todo  A {@link Todo} entry to log
         *
         * @return Logging {@link PresentationField}
         **/

        default PresentationField todo(String name, Todo todo) {
            return object(
                    name,
                    value("id", Value.string(todo.getId())),
                    value("title", Value.string(todo.getTitle())),
                    value("description", Value.string(todo.getDescription()))
            );
        }
    }
}
