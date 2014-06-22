package ru.lanwen.tests.jersey.heroku;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import ru.sharmana.resources.RootResource;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class RootResourceTest extends JerseyTest {


    @Override
    protected Application configure() {
        return new ResourceConfig(RootResource.class);
    }

    @Test
    public void shouldReturn404OnEmptyPath() {
    }
}
