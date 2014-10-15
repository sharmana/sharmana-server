package ru.sharmana;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import javax.ws.rs.ApplicationPath;
import java.util.logging.Logger;

/**
 * User: lanwen
 * Date: 13.10.14
 * Time: 17:09
 */
@ApplicationPath("/")
public class SharmanaApplication extends ResourceConfig {

    private static final Logger LOGGER = Logger.getLogger(SharmanaApplication.class.getName());

    public SharmanaApplication() {
        packages(true, "ru.sharmana");

        register(new LoggingFilter(LOGGER, true));
//        property(ServerProperties.TRACING, "ALL");
//        property(ServerProperties.TRACING_THRESHOLD, "VERBOSE");
    }
}
