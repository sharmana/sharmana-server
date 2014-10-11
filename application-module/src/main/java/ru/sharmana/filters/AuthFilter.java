package ru.sharmana.filters;

import org.eclipse.jetty.http.HttpStatus;
import ru.sharmana.beans.Error;
import ru.sharmana.misc.DBActions;
import ru.sharmana.resources.UserResource;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * User: lanwen
 * Date: 11.10.14
 * Time: 20:57
 */
@PreMatching
public class AuthFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String auth = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (requestContext.getUriInfo().getPath().equals("")
                || requestContext.getUriInfo().getPath().startsWith("user/auth")) {
            return;
        }

        if (auth == null || DBActions.getCollection(UserResource.USERS_COLLECTION).count("{_id: #}", auth) == 0) {
            requestContext.abortWith(Response.status(HttpStatus.UNAUTHORIZED_401)
                    .entity(new Error().withError("Wrong auth key: " + auth)).build());
        }
    }
}
