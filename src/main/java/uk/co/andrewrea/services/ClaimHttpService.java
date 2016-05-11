package uk.co.andrewrea.services;

import com.google.gson.Gson;
import spark.Service;
import uk.co.andrewrea.domain.dtos.ClaimDto;
import uk.co.andrewrea.domain.events.ClaimRegisteredEvent;
import uk.co.andrewrea.events.Publisher;

/**
 * Created by vagrant on 5/11/16.
 */
public class ClaimHttpService {
    public static final String NAME = "ClaimHttpService";
    private Service server;
    private Publisher publisher;

    public ClaimHttpService(Service server, Publisher publisher) {
        this.server = server;

        this.publisher = publisher;
    }

    public void start() {
        //TODO: Health check to ensure the Exchange is present in RabbitMQ
        //TODO: Circuit Breaker to retry the connection to RABBITMQ if the exchange is not present

        this.server.post("claims", (req, res) -> {
            ClaimDto claim = new Gson().fromJson(req.body(), ClaimDto.class);
            ClaimRegisteredEvent claimRegisteredEvent = new ClaimRegisteredEvent();
            claimRegisteredEvent.firstname = claim.firstname;
            claimRegisteredEvent.middlenames = claim.middlenames;
            claimRegisteredEvent.surname = claim.surname;
            claimRegisteredEvent.dob = claim.dob;
            claimRegisteredEvent.nino = claim.nino;
            claimRegisteredEvent.income = claim.income;

            publisher.publish(claimRegisteredEvent, ClaimRegisteredEvent.NAME);

            res.status(202);
            return "";
        });
    }

    public void stop() {
        this.server.stop();
    }
}
