package uk.co.andrewrea.events;


import java.io.IOException;

/**
 * Created by vagrant on 5/9/16.
 */
public interface Publisher {
    <T> void publish(T message, String eventType) throws IOException;
}
