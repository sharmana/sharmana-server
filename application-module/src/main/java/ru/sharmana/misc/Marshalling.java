package ru.sharmana.misc;

import org.jongo.ReflectiveObjectIdUpdater;
import org.jongo.marshall.jackson.JacksonIdFieldSelector;
import org.jongo.marshall.jackson.JacksonMapper;

/**
 * User: lanwen
 * Date: 11.10.14
 * Time: 15:46
 */
public class Marshalling {

    public static String marshall(Object obj) {
        return new JacksonMapper.Builder()
                .withObjectIdUpdater(new ReflectiveObjectIdUpdater(new JacksonIdFieldSelector()))
                .build().getMarshaller().marshall(obj).toString();
    }
}
