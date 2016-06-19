package uk.co.andrewrea.claim.query.updater.services;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.gson.Gson;
import com.rabbitmq.client.*;
import spark.Service;
import uk.co.andrewrea.claim.query.core.ClaimQueryService;
import uk.co.andrewrea.claim.query.domain.events.subscribe.ClaimAwardPaidEvent;
import uk.co.andrewrea.claim.query.domain.events.subscribe.ClaimAwardedEvent;
import uk.co.andrewrea.claim.query.domain.events.subscribe.ClaimRegisteredEvent;
import uk.co.andrewrea.claim.query.domain.events.subscribe.ClaimVerifiedEvent;
import uk.co.andrewrea.claim.query.updater.config.ClaimQueryUpdaterServiceConfiguration;
import uk.co.andrewrea.claim.query.updater.core.EventHandler;
import uk.co.andrewrea.claim.query.updater.eventHandlers.ClaimAwardPaidEventHandler;
import uk.co.andrewrea.claim.query.updater.eventHandlers.ClaimAwardedEventHandler;
import uk.co.andrewrea.claim.query.updater.eventHandlers.ClaimRegisteredEventHandler;
import uk.co.andrewrea.claim.query.updater.eventHandlers.ClaimVerifiedEventHandler;
import uk.co.andrewrea.infrastructure.inproc.Retry;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 6/19/16.
 */
public class ClaimQueryUpdaterHttpService {
    private Service service;
    private final HealthCheckRegistry healthChecks = new HealthCheckRegistry();
    private ClaimQueryUpdaterServiceConfiguration config;
    private ClaimQueryService claimService;
    private Map<String, EventHandler> eventHandlers;

    public ClaimQueryUpdaterHttpService(ClaimQueryUpdaterServiceConfiguration config, ClaimQueryService claimQueryService){
        this.config = config;
        this.claimService = claimQueryService;
        this.eventHandlers = new HashMap<>();
        this.eventHandlers.put(ClaimRegisteredEvent.NAME, new ClaimRegisteredEventHandler(claimQueryService));
        this.eventHandlers.put(ClaimVerifiedEvent.NAME, new ClaimVerifiedEventHandler(claimQueryService));
        this.eventHandlers.put(ClaimAwardedEvent.NAME, new ClaimAwardedEventHandler(claimQueryService));
        this.eventHandlers.put(ClaimAwardPaidEvent.NAME, new ClaimAwardPaidEventHandler(claimQueryService));

        this.healthChecks.register("application", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });
    }

    public void start() throws IOException, TimeoutException {
        startRabbitMQ();

        this.service = Service.ignite().port(config.servicePort).ipAddress(config.serviceIp);

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

    private void startRabbitMQ() throws IOException, TimeoutException {
        //Create a connection
        Connection conn = Retry.io(() -> {
            //Create a connection
            ConnectionFactory factory = new ConnectionFactory();
            factory.setVirtualHost("/");
            factory.setHost(this.config.amqpHost);
            factory.setPort(this.config.amqpPort);
            factory.setAutomaticRecoveryEnabled(true);

            return factory.newConnection();
        });
        Channel channel = conn.createChannel();

        //Create the host exchange
        channel.exchangeDeclare(this.config.claimQueryUpdaterExchangeName, "topic", false);

        //Create a queue and bind to the exchange
        String queueName = String.format("%s.%s","input", this.config.claimQueryUpdaterExchangeName);
        channel.queueDeclare(queueName, false, false, false, null);

        channel.queueBind(queueName, this.config.claimAwardServiceExchangeName, ClaimAwardedEvent.NAME);
        channel.queueBind(queueName, this.config.claimRegistrationServiceExchangeName, ClaimRegisteredEvent.NAME);
        channel.queueBind(queueName, this.config.claimFraudServiceExchangeName, ClaimVerifiedEvent.NAME);
        channel.queueBind(queueName, this.config.claimPaymentServiceExchangeName, ClaimAwardPaidEvent.NAME);


        //Create a consumer of the queue
        Runnable consumer = () -> {
            try {
                channel.basicConsume(queueName, false,
                        new DefaultConsumer(channel) {
                            @Override
                            public void handleDelivery(String consumerTag,
                                                       Envelope envelope,
                                                       AMQP.BasicProperties properties,
                                                       byte[] body)
                                    throws IOException {
                                String routingKey = envelope.getRoutingKey();
                                EventHandler eventHandler = eventHandlers.get(routingKey);
                                eventHandler.handle(body);
                                long deliveryTag = envelope.getDeliveryTag();
                                channel.basicAck(deliveryTag, false);
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        consumer.run();
    }

}
