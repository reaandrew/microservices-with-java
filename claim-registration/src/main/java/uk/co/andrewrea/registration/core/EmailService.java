package uk.co.andrewrea.registration.core;

/**
 * Created by vagrant on 5/21/16.
 */
public interface EmailService {
    void sendEmail(String to, String from, String content);
}
