package ru.sharmana.resources;

import com.mongodb.MongoClient;
import org.eclipse.jetty.http.HttpStatus;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import ru.sharmana.beans.*;
import ru.sharmana.beans.Error;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static ru.sharmana.misc.Marshalling.marshall;
import static ru.sharmana.misc.Props.props;

@Path("user")
public class UserResource {

    public static final String USERS_COLLECTION = "users";

    @POST
    @Path("add")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(@QueryParam("name") String name, @QueryParam("email") String email) throws UnknownHostException {
        MongoClient client = new MongoClient(props().getMongoUri().getHost(), props().getMongoUri().getPort());
        Jongo jongo = new Jongo(client.getDB(props().getDbName()));


        MongoCollection users = jongo.getCollection(USERS_COLLECTION);

        if(users.find("{email:#}", email).as(User.class).count() > 0) {
            return Response.status(HttpStatus.CONFLICT_409)
                    .entity(marshall(new Error().withError("user already exists"))).build();
        }

        users.save(new User().withName(name).withEmail(email));

        return Response.status(HttpStatus.CREATED_201).build();
    }

    @GET
    @Path("all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response all() throws UnknownHostException {
        MongoClient client = new MongoClient(props().getMongoUri().getHost(), props().getMongoUri().getPort());
        Jongo jongo = new Jongo(client.getDB(props().getDbName()));

        MongoCollection users = jongo.getCollection(USERS_COLLECTION);
        MongoCursor<User> result = users.find().as(User.class);

        List<User> toMarshall = new ArrayList<>();
        for (User user : result) {
            toMarshall.add(user);
        }
        return Response.status(HttpStatus.OK_200).
                entity(marshall(toMarshall)).build();
    }


}
