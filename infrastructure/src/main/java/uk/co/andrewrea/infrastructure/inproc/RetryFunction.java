package uk.co.andrewrea.infrastructure.inproc;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/26/16.
 */
public interface RetryFunction<T> {
    T execute() throws IOException, TimeoutException;
}
