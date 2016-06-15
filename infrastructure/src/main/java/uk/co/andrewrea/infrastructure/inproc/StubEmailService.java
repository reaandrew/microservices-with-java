package uk.co.andrewrea.infrastructure.inproc;


import uk.co.andrewrea.infrastructure.core.EmailService;

import java.util.ArrayList;

/**
 * Created by vagrant on 5/21/16.
 */
public class StubEmailService implements EmailService {

    protected class Email {
        private final String to;
        private final String body;

        public Email(String to, String body) {

            this.to = to;
            this.body = body;
        }

        public String getTo() {
            return to;
        }

        public String getBody() {
            return body;
        }
    }

    private ArrayList<Email> emails = new ArrayList<>();

    @Override
    public void sendEmail(String to, String from, String content) {
        emails.add(new Email(to, content));
    }

    public boolean emailSent(String to, String body) {
        return this.emails.stream().anyMatch(email -> email.getTo().equals(to) && email.getBody().equals(body));
    }
}
