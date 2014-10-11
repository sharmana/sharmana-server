package ru.sharmana.misc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * User: lanwen
 * Date: 11.10.14
 * Time: 15:46
 */
public class Marshalling {

    public static String marshall(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cant marshall", e);
        }
    }
}
