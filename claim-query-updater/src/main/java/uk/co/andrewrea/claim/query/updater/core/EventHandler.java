package uk.co.andrewrea.claim.query.updater.core;

/**
 * Created by vagrant on 6/19/16.
 */
public interface EventHandler {
    void handle(byte[] data);
}
