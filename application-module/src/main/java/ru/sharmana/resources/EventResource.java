package ru.sharmana.resources;

import jersey.repackaged.com.google.common.base.Preconditions;
import org.eclipse.jetty.http.HttpStatus;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import ru.sharmana.beans.Event;
import ru.sharmana.beans.Transaction;
import ru.sharmana.beans.User;
import ru.sharmana.misc.DBActions;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

import static jersey.repackaged.com.google.common.collect.Lists.newArrayList;
import static ru.sharmana.misc.DataActions.mergeTransactions;

@Path("event")
public class EventResource {

    public static final String EVENTS_COLLECTION = "events";

    @PUT
    @Path("add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addEvent(Event event) throws IOException {
        Preconditions.checkNotNull(event);

        MongoCollection dbEvents = DBActions.getCollection(EVENTS_COLLECTION);
        if(event.getId() != null) {
            Event writed = DBActions.selectById(dbEvents, event.getId(), Event.class);
            if (writed == null) {
                dbEvents.insert(event);
                return Response.status(HttpStatus.CREATED_201).entity(event).build();
            }

            List<Transaction> merged = mergeTransactions(writed.getTransactions(), event.getTransactions());
            writed.setTransactions(merged);
            dbEvents.save(writed);
            return Response.ok(writed).build();
        }
        dbEvents.insert(event);

        return Response.status(HttpStatus.CREATED_201).entity(event).build();
    }

    @GET
    @Path("my")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Event> myEvents(@HeaderParam(HttpHeaders.AUTHORIZATION) String token) {
        MongoCollection events = DBActions.getCollection(EVENTS_COLLECTION);

        User current = DBActions.selectById(DBActions.getCollection(UserResource.USERS_COLLECTION), token, User.class);
        MongoCursor<Event> result = events.find("{emails:{$regex: #}}}", current.getEmail()).as(Event.class);

        return newArrayList(result.iterator());
    }

}