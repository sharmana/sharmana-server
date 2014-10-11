package ru.sharmana.resources;

import jersey.repackaged.com.google.common.base.Preconditions;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import ru.sharmana.beans.Event;
import ru.sharmana.beans.User;
import ru.sharmana.misc.DBActions;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

import static jersey.repackaged.com.google.common.collect.Lists.newArrayList;

@Path("event")
public class EventResource {

    public static final String EVENTS_COLLECTION = "events";

    @PUT
    @Path("add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Event addGroup(Event event) throws IOException {
        Preconditions.checkNotNull(event);

        MongoCollection dbEvents = DBActions.getCollection(EVENTS_COLLECTION);
        dbEvents.insert(event);

        return event;
    }

    @GET
    @Path("my")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Event> allFor(@HeaderParam(HttpHeaders.AUTHORIZATION) String token) {
        MongoCollection events = DBActions.getCollection(EVENTS_COLLECTION);

        String auth = "54395f4130047d0c16c205df";

        User current = DBActions.selectById(DBActions.getCollection(UserResource.USERS_COLLECTION), auth, User.class);
        MongoCursor<Event> result = events.find("{emails:{$regex: #}}}", current.getEmail()).as(Event.class);

        return newArrayList(result.iterator());
    }


}
