package ru.sharmana.resources;

import ch.lambdaj.collection.LambdaIterable;
import ch.lambdaj.collection.LambdaList;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import jersey.repackaged.com.google.common.base.Preconditions;
import org.eclipse.jetty.http.HttpStatus;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matchers;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import ru.sharmana.beans.Checkout;
import ru.sharmana.beans.Event;
import ru.sharmana.beans.Status;
import ru.sharmana.beans.SumEachCheckout;
import ru.sharmana.beans.Transaction;
import ru.sharmana.beans.User;
import ru.sharmana.misc.DBActions;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.collection.LambdaCollections.with;
import static com.google.common.collect.FluentIterable.from;
import static java.lang.String.format;
import static javax.ws.rs.core.UriBuilder.fromUri;
import static jersey.repackaged.com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static ru.sharmana.misc.DBActions.getCollection;
import static ru.sharmana.misc.DataActions.mergeTransactions;

@Path("")
public class EventResource {

    public static final String EVENTS_COLLECTION = "events";

    @Context
    private UriInfo baseUri;


    @POST
    @Path("event/checkout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkoutEvent(Event event) {
        Preconditions.checkNotNull(event);
        Preconditions.checkNotNull(event.getId());

        return Response.status(HttpStatus.OK_200).entity(event.withCheckouts(checkout(event, baseUri))).build();
    }

    public static FluentIterable<SumEachCheckout> doubts(Event event) {
        List<SumEachCheckout> checkouts = new ArrayList<>();
        Double total = 0.0;

        for(String email : event.getEmails()) {
            FluentIterable<Double> toSum = from(event.getTransactions()).filter(email(email)).transform(extractCount());
            Double sum = sum(toSum);
            total = total + sum;
            checkouts.add(new SumEachCheckout().withCount(sum).withEmail(email));
        }

        Double each = total / checkouts.size();
        return from(checkouts).transform(doubts(each));
    }

    public static List<Checkout> checkout(Event event, UriInfo baseUri) {
        LambdaIterable<SumEachCheckout> doubts = with(doubts(event)).clone();

        List<Checkout> totally = new ArrayList<>();

        while (doubts.iterator().hasNext()) {
            LambdaList<SumEachCheckout> sorted = with(newArrayList(doubts)).clone()
                    .sort(on(SumEachCheckout.class).getCount());

            SumEachCheckout minimal = sorted.get(0);
            Collections.reverse(sorted);
            SumEachCheckout maximum = sorted.get(0);

            if (minimal.getCount().equals(maximum.getCount())) {
                break;
            } else if(Math.abs(minimal.getCount()) <= maximum.getCount()) {
                totally.add(new Checkout()
                        .withYamoneyUrl(fromUri(baseUri.getBaseUri()).path("pay")
                                .queryParam("amount", Math.round(Math.abs(minimal.getCount())))
                                .queryParam("transaction_name", format("%s (%s)", event.getName(), minimal.getEmail()))
                                .build().toString())
                        .withWho(minimal.getEmail())
                        .withTo(maximum.getEmail())
                        .withCount(Math.abs(minimal.getCount())));

                maximum.withCount(maximum.getCount() + minimal.getCount());
                doubts = doubts.remove(equalTo(minimal));


            } else {
                totally.add(new Checkout()
                        .withYamoneyUrl(fromUri(baseUri.getBaseUri()).path("pay")
                                .queryParam("amount", Math.round(Math.abs(minimal.getCount())))
                                .queryParam("transaction_name", format("%s (%s)", event.getName(), minimal.getEmail()))
                                .build().toString())
                        .withWho(minimal.getEmail())
                        .withTo(maximum.getEmail())
                        .withCount(maximum.getCount()
                        ));

                doubts = doubts.remove(equalTo(maximum));
                minimal.withCount(minimal.getCount() + maximum.getCount());
                maximum.withCount(0.0);
            }

            doubts = doubts.remove(having(on(SumEachCheckout.class).getCount(), Matchers.equalTo(0.0)));
        }

        return totally;
    }

    public static FeatureMatcher<SumEachCheckout, Boolean> equalTo(final SumEachCheckout minimal) {
        return new FeatureMatcher<SumEachCheckout, Boolean>(is(true), "", "") {
            @Override
            protected Boolean featureValueOf(SumEachCheckout actual) {
                return actual.getCount().equals(minimal.getCount())
                        && actual.getEmail().equals(minimal.getEmail());
            }
        };
    }

    public static Function<SumEachCheckout, SumEachCheckout> doubts(final Double each) {
        return new Function<SumEachCheckout, SumEachCheckout>() {
            @Override
            public SumEachCheckout apply(SumEachCheckout input) {
                return input.withCount(input.getCount() - each);
            }
        };
    }

    public static Function<Transaction, Double> extractCount() {
        return new Function<Transaction, Double>() {
            @Override
            public Double apply(Transaction tr) {
                return tr.getCount();
            }
        };
    }

    public static Double sum(Iterable<Double> counts) {
        Double result = 0.0;
        for (Double one : counts) {
            result = result + one;
        }
        return result;
    }

    public static Predicate<Transaction> email(final String email) {
        return new Predicate<Transaction>() {
            @Override
            public boolean apply(Transaction input) {
                return email.equals(input.getWho());
            }
        };
    }


    @PUT
    @Path("event/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addEvent(Event event, @HeaderParam(HttpHeaders.AUTHORIZATION) String token) {
        Preconditions.checkNotNull(event);
        HashSet<String> emails = new HashSet<>(event.getEmails());
        User current = DBActions.selectById(getCollection(UserResource.USERS_COLLECTION), token, User.class);
        emails.add(current.getEmail());

        MongoCollection dbEvents = getCollection(EVENTS_COLLECTION);
        if (event.getId() != null) {
            Event writed = DBActions.selectById(dbEvents, event.getId(), Event.class);
            if (writed == null) {
                dbEvents.save(event.withEmails(newArrayList(emails)));
                return Response.status(HttpStatus.CREATED_201).entity(event).build();
            }

            List<Transaction> merged = mergeTransactions(writed.getTransactions(), event.getTransactions());
            writed.setTransactions(merged);

            dbEvents.save(writed.withEmails(newArrayList(emails)));
            return Response.ok(writed).build();
        }
        dbEvents.save(event.withEmails(newArrayList(emails)));
        return Response.status(HttpStatus.CREATED_201).entity(event).build();
    }

    /**
     * Логика тут следующая: берем пришедшие эвенты и фильтруем те у которых есть идешники.
     * Далее запрашиваем из базы эти идешники.
     * С приехавшими эвентами из базы (их может быть меньше) мержим новые с новыми транзакциями.
     * Далее те которых в базе не оказалось причисляем к новым, добавляем
     * к ним те которые без идешников и инсертим в базу
     *
     * @param events
     *
     * @return
     * @throws IOException
     */
    @PUT
    @Path("events/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addEvents(List<Event> events, @HeaderParam(HttpHeaders.AUTHORIZATION) String token) {
        Preconditions.checkNotNull(events);

        MongoCollection dbEvents = getCollection(EVENTS_COLLECTION);

        final ImmutableListMultimap<String, Event> ids = from(events)
                .filter(hasId())
                .index(extractId());

        MongoCursor<Event> writed = dbEvents.find("{_id:{$in:#}}", ids.keys()).as(Event.class);

        List<Event> toMerge = new ArrayList<>();

        for (Event exists : writed) {
            exists.setTransactions(mergeTransactions(exists.getTransactions(),
                    ids.get(exists.getId()).get(0).getTransactions()));
            toMerge.add(exists);
        }

        dbEvents.save(toMerge);

        final ImmutableListMultimap<String, Event> idsMerged = from(toMerge)
                .index(extractId());

        ImmutableList<Event> newEvents = from(events).filter(Predicates.not(hasId()))
                .append(from(events).filter(new Predicate<Event>() {
                    @Override
                    public boolean apply(Event input) {
                        return !idsMerged.containsKey(input.getId());
                    }
                })).toList();


        dbEvents.save(newEvents);

        return Response.status(HttpStatus.CREATED_201)
                .entity(new Status().withCreated((long) newEvents.size())
                        .withUpdated((long) toMerge.size())).build();
    }

    private Function<Event, String> extractId() {
        return new Function<Event, String>() {
            @Override
            public String apply(Event input) {
                return input.getId();
            }
        };
    }

    private Predicate<Event> hasId() {
        return new Predicate<Event>() {
            @Override
            public boolean apply(Event input) {
                return input.getId() != null;
            }
        };
    }

    @GET
    @Path("events/my")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Event> myEvents(@HeaderParam(HttpHeaders.AUTHORIZATION) String token) {
        MongoCollection events = getCollection(EVENTS_COLLECTION);

        User current = DBActions.selectById(getCollection(UserResource.USERS_COLLECTION), token, User.class);
        MongoCursor<Event> result = events.find("{emails:{$regex: #}}}", current.getEmail()).as(Event.class);

        return newArrayList(result.iterator());
    }

}