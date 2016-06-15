package uk.co.andrewrea.infrastructure.inproc;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/26/16.
 */
public class Retry {

    public static final <T> T io(RetryFunction<T> retry) throws IOException, TimeoutException {
        int current = 1;
        int max = 10;

        T returnObject = null;
        while (current < max) {

            try {
                returnObject = retry.execute();
                System.out.println(String.format("retry succeeded after %d seconds", current / 2));
                break;
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
                System.out.println(String.format("continuing in %d seconds", current));
                int nextCurrent = 0;
                if (current + current < max) {
                    nextCurrent = current + current;
                } else {
                    nextCurrent = current;
                }
                try {
                    Thread.sleep(current * 1000);
                    current = nextCurrent;
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return returnObject;

    }
}
