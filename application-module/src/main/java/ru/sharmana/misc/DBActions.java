package ru.sharmana.misc;

import com.mongodb.MongoClient;
import org.jongo.Jongo;
import org.jongo.MongoCollection;

import static ru.sharmana.misc.Props.props;

/**
 * User: lanwen
 * Date: 11.10.14
 * Time: 19:30
 */
public class DBActions {
    public static MongoCollection getCollection(String collection) {
        MongoClient client = new MongoClient(props().getMongoServerAddress());
        Jongo jongo = new Jongo(client.getDB(props().getDbName()));

        return jongo.getCollection(collection);
    }
}
