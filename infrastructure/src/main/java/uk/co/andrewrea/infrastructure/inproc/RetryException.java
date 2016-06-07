package uk.co.andrewrea.infrastructure.inproc;

/**
 * Created by vagrant on 5/26/16.
 */
public class RetryException extends RuntimeException {
    public RetryException(Throwable cause){
        super(cause);
    }
}
