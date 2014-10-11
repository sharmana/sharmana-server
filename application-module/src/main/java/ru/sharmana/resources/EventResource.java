package ru.sharmana.resources;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import jersey.repackaged.com.google.common.base.Preconditions;
import org.eclipse.jetty.http.HttpStatus;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import ru.sharmana.beans.Event;
import ru.sharmana.beans.Status;
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
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;
import static jersey.repackaged.com.google.common.collect.Lists.newArrayList;
import static ru.sharmana.misc.DataActions.mergeTransactions;

@Path("")
public class EventResource {

    public static final String EVENTS_COLLECTION = "events";

    @PUT
    @Path("event/add")
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

    @PUT
    @Path("events/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addEvents(List<Event> events) throws IOException {
        Preconditions.checkNotNull(events);

        MongoCollection dbEvents = DBActions.getCollection(EVENTS_COLLECTION);

        final ImmutableListMultimap<String, Event> ids = from(events)
                .filter(hasId())
                .index(extractId());

        MongoCursor<Event> writed = dbEvents.find("{_id:{$in:#}}", ids.keys()).as(Event.class);

        List<Event> toMerge = new ArrayList<>();

        for (Event exists : writed) {
             exists.setTransactions(mergeTransactions(exists.getTransactions(),
                     ids.get(exists.getId()).get(0).getTransactions()));
            toMerge.add(exists);
        }

        dbEvents.save(toMerge);

        final ImmutableListMultimap<String, Event> idsMerged = from(toMerge)
                .index(extractId());

        ImmutableList<Event> newEvents = from(events).filter(Predicates.not(hasId()))
                .append(from(events).filter(new Predicate<Event>() {
            @Override
            public boolean apply(Event input) {
                return !idsMerged.containsKey(input.getId());
            }
        })).toList();


        dbEvents.insert(newEvents);

        return Response.status(HttpStatus.CREATED_201)
                .entity(new Status().withCreated((long)newEvents.size())
                        .withUpdated((long)toMerge.size())).build();
    }

    private Function<Event, String> extractId() {
        return new Function<Event, String>() {
            @Override
            public String apply(Event input) {
                return input.getId();
            }
        };
    }

    private Predicate<Event> hasId() {
        return new Predicate<Event>() {
            @Override
            public boolean apply(Event input) {
                return input.getId() != null;
            }
        };
    }

    @GET
    @Path("events/my")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Event> myEvents(@HeaderParam(HttpHeaders.AUTHORIZATION) String token) {
        MongoCollection events = DBActions.getCollection(EVENTS_COLLECTION);

        User current = DBActions.selectById(DBActions.getCollection(UserResource.USERS_COLLECTION), token, User.class);
        MongoCursor<Event> result = events.find("{emails:{$regex: #}}}", current.getEmail()).as(Event.class);

        return newArrayList(result.iterator());
    }

}