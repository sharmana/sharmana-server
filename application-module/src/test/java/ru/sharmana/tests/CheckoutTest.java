package ru.sharmana.tests;

import ch.lambdaj.collection.LambdaIterable;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matcher;
import org.junit.Test;
import ru.sharmana.beans.Checkout;
import ru.sharmana.beans.Event;
import ru.sharmana.beans.SumEachCheckout;
import ru.sharmana.beans.Transaction;
import ru.sharmana.resources.EventResource;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.collection.LambdaCollections.with;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: lanwen
 * Date: 12.10.14
 * Time: 3:06
 */

public class CheckoutTest {

    public static Event getEvent1() {
        return new Event()
                .withEmails(asList("1", "2", "3", "4"))
                .withTransactions(asList(
                        new Transaction().withWho("1").withCount(40.0),
                        new Transaction().withWho("1").withCount(10.0),
                        new Transaction().withWho("2").withCount(0.0),
                        new Transaction().withWho("3").withCount(60.0),
                        new Transaction().withWho("4").withCount(10.0)
                ));
    }

    public static Event getEvent2() {
        return new Event()
                .withEmails(asList("1", "2", "3", "4"))
                .withTransactions(asList(
                        new Transaction().withWho("1").withCount(92.0),
                        new Transaction().withWho("1").withCount(10.0),
                        new Transaction().withWho("2").withCount(0.0),
                        new Transaction().withWho("3").withCount(60.0),
                        new Transaction().withWho("4").withCount(10.0)
                ));
    }

    @Test
    public void doubtShouldCount1() throws Exception {
        LambdaIterable<SumEachCheckout> doubts = with(EventResource.doubts(getEvent1())).clone();
        System.out.println(new ObjectMapper().writeValueAsString(doubts.clone()));

        assertThat(doubts, hasItems(
                (Matcher) having(on(SumEachCheckout.class).getCount(), equalTo(20.0)),
                (Matcher) having(on(SumEachCheckout.class).getCount(), equalTo(-30.0)),
                (Matcher) having(on(SumEachCheckout.class).getCount(), equalTo(30.0)),
                (Matcher) having(on(SumEachCheckout.class).getCount(), equalTo(-20.0))
        ));
    }

    @Test
    public void checkoutShouldCount1() throws Exception {
        UriInfo mock = mock(UriInfo.class);
        when(mock.getBaseUri()).thenReturn(URI.create("/"));
        List<Checkout> checkout = EventResource.checkout(getEvent1(), mock);
        System.out.println(new ObjectMapper().writeValueAsString(checkout));

        assertThat(checkout, hasSize(2));
        assertThat(checkout, hasItems(
                (Matcher) having(on(Checkout.class).getCount(), equalTo(30.0)),
                (Matcher) having(on(Checkout.class).getCount(), equalTo(20.0))
        ));
    }
   @Test
    public void doubtShouldCount2() throws Exception {
        LambdaIterable<SumEachCheckout> doubts =  with(EventResource.doubts(getEvent2())).clone();
        System.out.println(new ObjectMapper().writeValueAsString(doubts.clone()));

        assertThat(doubts, hasItems(
                (Matcher) having(on(SumEachCheckout.class).getCount(), equalTo(59.0)),
                (Matcher) having(on(SumEachCheckout.class).getCount(), equalTo(-43.0)),
                (Matcher) having(on(SumEachCheckout.class).getCount(), equalTo(17.0)),
                (Matcher) having(on(SumEachCheckout.class).getCount(), equalTo(-33.0))
        ));
    }

    @Test
    public void checkoutShouldCount2() throws Exception {
        UriInfo mock = mock(UriInfo.class);
        when(mock.getBaseUri()).thenReturn(URI.create("/"));
        List<Checkout> checkout = EventResource.checkout(getEvent2(), mock);
        System.out.println(new ObjectMapper().writeValueAsString(checkout));

        assertThat(checkout, hasSize(3));
        assertThat(checkout, hasItems(
                (Matcher) having(on(Checkout.class).getCount(), equalTo(43.0)),
                (Matcher) having(on(Checkout.class).getCount(), equalTo(16.0)),
                (Matcher) having(on(Checkout.class).getCount(), equalTo(17.0))
        ));
    }
}
