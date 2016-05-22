package uk.co.andrewrea.registration.services;

import com.google.gson.Gson;
import spark.Service;
import uk.co.andrewrea.infrastructure.core.Publisher;
import uk.co.andrewrea.registration.core.IdGenerator;
import uk.co.andrewrea.registration.domain.dtos.ClaimDto;
import uk.co.andrewrea.registration.domain.events.publish.ClaimRegisteredEvent;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/11/16.
 */
public class ClaimRegistrationHttpService {
    private IdGenerator idGenerator;
    private Service server;
    private Publisher publisher;

    public ClaimRegistrationHttpService(IdGenerator idGenerator, Service server, Publisher publisher) {
        this.idGenerator = idGenerator;
        this.server = server;
        this.publisher = publisher;
    }

    public void start() throws IOException, TimeoutException {
        //TODO: Health check to ensure the Exchange is present in RabbitMQ
        //TODO: Circuit Breaker to retry the connection to RABBITMQ if the exchange is not present

        this.server.post("claims", (req, res) -> {
            ClaimDto claim = new Gson().fromJson(req.body(), ClaimDto.class);
            ClaimRegisteredEvent claimRegisteredEvent = new ClaimRegisteredEvent();
            claimRegisteredEvent.id = this.idGenerator.generateID();
            claimRegisteredEvent.claim = claim;


            publisher.publish(claimRegisteredEvent, ClaimRegisteredEvent.NAME);

            res.status(202);
            return "";
        });
    }

    public void stop() {
        this.server.stop();
    }
}
