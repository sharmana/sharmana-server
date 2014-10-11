package ru.sharmana.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import ru.sharmana.beans.Event;
import ru.sharmana.beans.Transaction;
import ru.sharmana.resources.EventResource;

import static java.util.Arrays.asList;

/**
 * User: lanwen
 * Date: 12.10.14
 * Time: 3:06
 */

public class CheckoutTest {

    @Test
    public void testName() throws Exception {

        Event event = new Event()
                .withEmails(asList("1", "2", "3", "4"))
                .withTransactions(asList(
                        new Transaction().withWho("1").withCount(40.0),
                        new Transaction().withWho("1").withCount(10.0),
                        new Transaction().withWho("2").withCount(0.0),
                        new Transaction().withWho("3").withCount(60.0),
                        new Transaction().withWho("4").withCount(10.0)
                ));

        System.out.println(new ObjectMapper().writeValueAsString(EventResource.doubts(event).iterator()));
        System.out.println(new ObjectMapper().writeValueAsString(EventResource.checkout(event)));
    }

    @Test
    public void testName2() throws Exception {

        Event event = new Event()
                .withEmails(asList("1", "2", "3", "4"))
                .withTransactions(asList(
                        new Transaction().withWho("1").withCount(92.0),
                        new Transaction().withWho("1").withCount(10.0),
                        new Transaction().withWho("2").withCount(0.0),
                        new Transaction().withWho("3").withCount(60.0),
                        new Transaction().withWho("4").withCount(10.0)
                ));

        System.out.println(new ObjectMapper().writeValueAsString(EventResource.doubts(event).iterator()));
        System.out.println(new ObjectMapper().writeValueAsString(EventResource.checkout(event)));
    }
}
