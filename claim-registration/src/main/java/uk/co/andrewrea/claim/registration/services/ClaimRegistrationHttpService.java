package uk.co.andrewrea.claim.registration.services;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.gson.Gson;
import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.StrictExceptionHandler;
import spark.Service;
import uk.co.andrewrea.claim.registration.config.ClaimRegistrationConfiguration;
import uk.co.andrewrea.claim.registration.domain.events.publish.ClaimRegisteredEvent;
import uk.co.andrewrea.claim.registration.domain.dtos.ClaimDto;
import uk.co.andrewrea.infrastructure.inproc.Retry;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/11/16.
 */
public class ClaimRegistrationHttpService {
    private Service service;
    private final HealthCheckRegistry healthChecks = new HealthCheckRegistry();
    private ClaimRegistrationConfiguration config;

    public ClaimRegistrationHttpService(ClaimRegistrationConfiguration configuration) {
        this.config = configuration;
        this.healthChecks.register("application", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });
    }

    public void start() throws IOException, TimeoutException {
        //TODO: Health check to ensure the Exchange is present in RabbitMQ

        this.service = Service.ignite().port(config.servicePort).ipAddress(config.serviceIp);

        Connection conn = Retry.io(() -> {
            //Create a connection
            ConnectionFactory factory = new ConnectionFactory();
            factory.setVirtualHost("/");
            factory.setHost(this.config.amqpHost);
            factory.setPort(this.config.amqpPort);
            factory.setUsername(this.config.amqpUsername);
            factory.setPassword(this.config.amqpPassword);
            factory.setAutomaticRecoveryEnabled(true);

            return factory.newConnection();
        });

        Channel channel = conn.createChannel();
        //Create the host exchange
        channel.exchangeDeclare(this.config.claimRegistrationServiceExchangeName, "topic", false);

        this.service.post("claims", (req, res) -> {
            ClaimDto claim = new Gson().fromJson(req.body(), ClaimDto.class);
            ClaimRegisteredEvent claimRegisteredEvent = new ClaimRegisteredEvent();
            claimRegisteredEvent.id = UUID.randomUUID().toString();
            claimRegisteredEvent.claim = claim;

            byte[] messageBodyBytes = new Gson().toJson(claimRegisteredEvent).getBytes();

            AMQP.BasicProperties messageProperties = new AMQP.BasicProperties.Builder()
                    .contentType("application/json")
                    .build();

            channel.basicPublish(config.claimRegistrationServiceExchangeName, ClaimRegisteredEvent.NAME, messageProperties, messageBodyBytes);

            res.status(202);
            return "";
        });

        this.service.get("/info", (req, res) -> {
            res.status(200);
            return "";
        });

        this.service.get("/health", (req, res) -> {
            res.status(200);

            return new Gson().toJson(healthChecks.runHealthChecks().values());
        });

        this.service.get("/metrics", (req, res) -> {
            res.status(200);
            return "";
        });
    }

    public void stop() {
        this.service.stop();
    }
}
