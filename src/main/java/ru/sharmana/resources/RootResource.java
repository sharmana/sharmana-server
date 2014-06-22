package ru.sharmana.resources;

import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.LoggerFactory;
import ru.sharmana.misc.Defaults;
import ru.sharmana.misc.Github;
import ru.sharmana.misc.Shield;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static java.lang.String.format;

@Path("")
public class RootResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response showHelp() {
        return Response.status(HttpStatus.NOT_FOUND_404).
                entity(format("Hello, SHARMANA API!")).type("text/plain").build();
    }
}
