package uk.co.andrewrea.registration.services;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.gson.Gson;
import spark.Service;
import uk.co.andrewrea.infrastructure.core.Publisher;
import uk.co.andrewrea.registration.core.IdGenerator;
import uk.co.andrewrea.registration.domain.dtos.ClaimDto;
import uk.co.andrewrea.registration.domain.events.publish.ClaimRegisteredEvent;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/11/16.
 */
public class ClaimRegistrationHttpService {
    private IdGenerator idGenerator;
    private Service server;
    private Publisher publisher;
    private final HealthCheckRegistry healthChecks = new HealthCheckRegistry();

    public ClaimRegistrationHttpService(Service server, Publisher publisher) {
        this.server = server;
        this.publisher = publisher;
        this.healthChecks.register("application", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });
    }

    public void start() {
        //TODO: Health check to ensure the Exchange is present in RabbitMQ
        //TODO: Circuit Breaker to retry the connection to RABBITMQ if the exchange is not present

        this.server.post("claims", (req, res) -> {
            ClaimDto claim = new Gson().fromJson(req.body(), ClaimDto.class);
            ClaimRegisteredEvent claimRegisteredEvent = new ClaimRegisteredEvent();
            claimRegisteredEvent.id = UUID.randomUUID().toString();
            claimRegisteredEvent.claim = claim;

            publisher.publish(claimRegisteredEvent, ClaimRegisteredEvent.NAME);

            res.status(202);
            return "";
        });

        this.server.get("/info",(req,res) -> {
            res.status(200);
            return "";
        } );

        this.server.get("/health",(req,res) -> {
            res.status(200);

            return new Gson().toJson(healthChecks.runHealthChecks().values());
        } );

        this.server.get("/metrics",(req,res) -> {
            res.status(200);
            return "";
        } );
    }

    public void stop() {
        this.server.stop();
    }
}
