package ru.sharmana.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.jetty.http.HttpStatus;
import org.jongo.MongoCollection;
import ru.sharmana.beans.User;
import ru.sharmana.beans.YandexLogin;
import ru.sharmana.misc.DBActions;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("user")
public class UserResource {

    public static final String USERS_COLLECTION = "users";

    @POST
    @Path("auth")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(@HeaderParam("Yandex-Token") String token) throws JsonProcessingException {
        MongoCollection users = DBActions.getCollection(USERS_COLLECTION);


        Response invoke = ClientBuilder.newClient().target("https://login.yandex.ru/info")
                .queryParam("format", "json")
                .queryParam("oauth_token", token).request().buildGet().invoke();


        YandexLogin login;
        if (invoke.getStatus() == HttpStatus.OK_200) {
            login = invoke.readEntity(YandexLogin.class);
            User user = users.findOne("{yandex_id:#}", login.getId()).as(User.class);

            if (user != null) {
                return Response.ok().entity(user).build();
            } else {
                user = new User()
                        .withEmail(login.getDefaultEmail())
                        .withYandexId(login.getId())
                        .withName(login.getLogin());
                users.insert(user);
                return Response.ok().entity(user).build();
            }
        }

        return invoke;
    }


}
