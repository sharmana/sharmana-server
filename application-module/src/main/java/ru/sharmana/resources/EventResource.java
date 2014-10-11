package ru.sharmana.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import ru.sharmana.beans.Event;
import ru.sharmana.misc.DBActions;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import static jersey.repackaged.com.google.common.collect.Lists.newArrayList;
import static ru.sharmana.misc.Props.props;

@Path("event")
public class EventResource {

    public static final String EVENTS_COLLECTION = "events";

    @PUT
    @Path("add")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Object> addGroup() throws IOException {
        MongoCollection users = DBActions.getCollection(EVENTS_COLLECTION);
//        Event pojo = new Event().withName(name).withEmails(emails).withCurrency(currency).withCreated(created);
//        users.save(pojo);

        return new ObjectMapper().reader(Event.class).readValues("[{\"name\":\"Поездка в Баварию\",\"currency\":\"руб\",\"created\":1413036541,\"emails\":[\"lanwen@yandex.ru\",\"some@yandex.ru\"],\"transactions\":[]},{\"_id\":\"54393d4f3004605fbc47df35\",\"name\":\"Поездка в Баварию\",\"currency\":\"руб\",\"created\":1413036542,\"emails\":[\"lanwen@yandex.ru\",\"some@yandex.ru\"],\"transactions\":[]},{\"_id\":\"543949df30045bfb81013770\",\"name\":\"Поездка в Баварию\",\"currency\":\"руб\",\"created\":1413036542,\"emails\":[\"lanwen@yandex.ru\",\"some@yandex.ru\"],\"transactions\":[]}]").readAll();
    }

    @GET
    @Path("my")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Event> allFor() throws UnknownHostException {
        MongoClient client = new MongoClient(props().getMongoServerAddress().getHost(), props().getMongoServerAddress().getPort());
        Jongo jongo = new Jongo(client.getDB(props().getDbName()));

        MongoCollection users = jongo.getCollection(EVENTS_COLLECTION);
        MongoCursor<Event> result = users.find().as(Event.class);

//        List<Event> toMarshall = new ArrayList<>();
//        for (Event event : result) {
//            toMarshall.add(event);
//        }
        return newArrayList(result.iterator());
    }


}
