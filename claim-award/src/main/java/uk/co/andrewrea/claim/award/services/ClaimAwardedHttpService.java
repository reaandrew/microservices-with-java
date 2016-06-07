package uk.co.andrewrea.claim.award.services;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.gson.Gson;
import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.StrictExceptionHandler;
import spark.Service;
import uk.co.andrewrea.claim.award.config.ClaimAwardServiceConfiguration;
import uk.co.andrewrea.claim.award.domain.events.publish.ClaimAwardedEvent;
import uk.co.andrewrea.claim.award.domain.events.subscribe.ClaimVerifiedEvent;
import uk.co.andrewrea.infrastructure.core.Publisher;
import uk.co.andrewrea.infrastructure.inproc.Retry;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/13/16.
 */
public class ClaimAwardedHttpService {
    private Service service;
    private ClaimAwardServiceConfiguration config;
    private final HealthCheckRegistry healthChecks = new HealthCheckRegistry();

    public ClaimAwardedHttpService(ClaimAwardServiceConfiguration config) {

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

        this.service.get("/info",(req,res) -> {
            res.status(200);
            return "";
        } );

        this.service.get("/health",(req,res) -> {
            res.status(200);

            return new Gson().toJson(healthChecks.runHealthChecks().values());
        } );

        this.service.get("/metrics",(req,res) -> {
            res.status(200);
            return "";
        } );

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
        channel.exchangeDeclare(this.config.claimAwardServiceExchangeName,"topic", false);

        //Create a queue and bind to the exchange
        String queueName = String.format("%s.%s",this.config.claimFraudServiceExchangeName, this.config.claimAwardServiceExchangeName);
        channel.queueDeclare(queueName,false, false, false, null);
        channel.queueBind(queueName, this.config.claimFraudServiceExchangeName, ClaimVerifiedEvent.NAME);

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
                                    throws IOException
                            {
                                ClaimVerifiedEvent claimVerifiedEvent = new Gson().fromJson(new String(body), ClaimVerifiedEvent.class);

                                ClaimAwardedEvent claimAwardedEvent = new ClaimAwardedEvent();
                                claimAwardedEvent.claim = claimVerifiedEvent.claim;

                                byte[] messageBodyBytes = new Gson().toJson(claimAwardedEvent).getBytes();

                                AMQP.BasicProperties messageProperties = new AMQP.BasicProperties.Builder()
                                        .contentType("application/json")
                                        .build();

                                channel.basicPublish(config.claimAwardServiceExchangeName, ClaimAwardedEvent.NAME, messageProperties, messageBodyBytes);

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

    public void stop() {
        this.service.stop();
    }
}
