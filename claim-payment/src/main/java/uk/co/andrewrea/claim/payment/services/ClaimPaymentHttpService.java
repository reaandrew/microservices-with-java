package uk.co.andrewrea.claim.payment.services;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.gson.Gson;
import com.rabbitmq.client.*;
import spark.Service;
import uk.co.andrewrea.claim.payment.config.ClaimPaymentServiceConfiguration;
import uk.co.andrewrea.claim.payment.domain.events.publish.ClaimAwardPaidEvent;
import uk.co.andrewrea.claim.payment.domain.events.subscribe.ClaimAwardedEvent;
import uk.co.andrewrea.infrastructure.core.Publisher;
import uk.co.andrewrea.infrastructure.inproc.Retry;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/13/16.
 */
public class ClaimPaymentHttpService {
    private Service service;
    private final HealthCheckRegistry healthChecks = new HealthCheckRegistry();
    private ClaimPaymentServiceConfiguration config;

    public ClaimPaymentHttpService(ClaimPaymentServiceConfiguration config) {
        this.config = config;
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
            factory.setUsername(this.config.amqpUsername);
            factory.setPassword(this.config.amqpPassword);
            factory.setAutomaticRecoveryEnabled(true);

            return factory.newConnection();
        });
        Channel channel = conn.createChannel();

        //Create the host exchange
        channel.exchangeDeclare(this.config.claimPaymentServiceExchangeName, "topic", false);

        //Create a queue and bind to the exchange
        String queueName = String.format("%s.%s", this.config.claimAwardServiceExchangeName, this.config.claimPaymentServiceExchangeName);
        channel.queueDeclare(queueName, false, false, false, null);
        channel.queueBind(queueName, this.config.claimAwardServiceExchangeName, ClaimAwardedEvent.NAME);

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
                                ClaimAwardedEvent claimAwardedEvent = new Gson().fromJson(new String(body), ClaimAwardedEvent.class);

                                ClaimAwardPaidEvent claimAwardPaidEvent = new ClaimAwardPaidEvent();
                                claimAwardPaidEvent.id = claimAwardedEvent.id;
                                claimAwardPaidEvent.claim = claimAwardedEvent.claim;

                                byte[] messageBodyBytes = new Gson().toJson(claimAwardPaidEvent).getBytes();

                                AMQP.BasicProperties messageProperties = new AMQP.BasicProperties.Builder()
                                        .contentType("application/json")
                                        .build();

                                channel.basicPublish(config.claimPaymentServiceExchangeName, claimAwardPaidEvent.NAME, messageProperties, messageBodyBytes);

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
