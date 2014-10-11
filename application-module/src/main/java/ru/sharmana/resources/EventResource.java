package ru.sharmana.resources;

import com.mongodb.MongoClient;
import jersey.repackaged.com.google.common.base.Preconditions;
import org.eclipse.jetty.http.HttpStatus;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import ru.sharmana.beans.Event;

import javax.validation.Validation;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static ru.sharmana.misc.Marshalling.marshall;
import static ru.sharmana.misc.Props.props;

@Path("event")
public class EventResource {

    public static final String EVENTS_COLLECTION = "events";

    @POST
    @Path("add")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addGroup(@FormParam("emails") List<String> emails,
                             @FormParam("name") String name,
                             @FormParam("currency") String currency,
                             @FormParam("created") Long created) throws UnknownHostException {
        MongoClient client = new MongoClient(props().getMongoUri().getHost(), props().getMongoUri().getPort());
        Jongo jongo = new Jongo(client.getDB(props().getDbName()));

        Preconditions.checkNotNull(emails);
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(currency);
        Preconditions.checkNotNull(created);

        MongoCollection users = jongo.getCollection(EVENTS_COLLECTION);
        users.save(new Event().withName(name).withEmails(emails).withCurrency(currency).withCreated(created));

        return Response.status(HttpStatus.CREATED_201).build();
    }

    @GET
    @Path("my")
    @Produces(MediaType.APPLICATION_JSON)
    public Response allFor() throws UnknownHostException {
        MongoClient client = new MongoClient(props().getMongoUri().getHost(), props().getMongoUri().getPort());
        Jongo jongo = new Jongo(client.getDB(props().getDbName()));

        MongoCollection users = jongo.getCollection(EVENTS_COLLECTION);
        MongoCursor<Event> result = users.find().as(Event.class);

        List<Event> toMarshall = new ArrayList<>();
        for (Event event : result) {
            toMarshall.add(event);
        }
        return Response.status(HttpStatus.OK_200).
                entity(marshall(toMarshall)).build();
    }


}
