package ru.sharmana.misc;

import com.mongodb.ServerAddress;
import ru.yandex.qatools.properties.PropertyLoader;
import ru.yandex.qatools.properties.annotations.Property;

import java.net.URI;
import java.net.UnknownHostException;

/**
 * User: lanwen
 * Date: 11.10.14
 * Time: 13:11
 */
public class Props {

    private Props() {
        PropertyLoader.populate(this);
    }

    private static Props instanse;

    public static Props props() {
        if (instanse == null) {
            instanse = new Props();
        }
        return instanse;
    }

    @Property("mongo.uri")
    private URI mongoUri = URI.create("http://127.0.0.1:27017");

    @Property("mongo.dbname")
    private String dbName = "sharmana";


    @Property("client.id.key")
    private String clientId = "";


    @Property("yandex.money.api.template")
    private String yaMoneyApiTemplate = "https://money.yandex.ru/api/{method}";


    @Property("sharmana.application.uri.success")
    private String sharmanaAppUriSuccess = "sharmana://success";


    @Property("sharmana.application.uri.failed")
    private String sharmanaAppUriFailed = "sharmana://failed";




    public ServerAddress getMongoServerAddress() {
        try {
            return new ServerAddress(mongoUri.getHost(), mongoUri.getPort());
        } catch (UnknownHostException e) {
            throw new RuntimeException("Can't read mongodb host and port", e);
        }
    }

    public String getDbName() {
        return dbName;
    }

    public String getClientId() {
        return clientId;
    }

    public String getYaMoneyApiTemplate() {
        return yaMoneyApiTemplate;
    }

    public String getSharmanaAppUriSuccess() {
        return sharmanaAppUriSuccess;
    }

    public String getSharmanaAppUriFailed() {
        return sharmanaAppUriFailed;
    }
}
