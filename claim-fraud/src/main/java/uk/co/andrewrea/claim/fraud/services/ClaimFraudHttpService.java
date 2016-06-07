package uk.co.andrewrea.claim.fraud.services;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.gson.Gson;
import com.rabbitmq.client.*;
import spark.Service;
import uk.co.andrewrea.claim.fraud.config.ClaimFraudServiceConfiguration;
import uk.co.andrewrea.claim.fraud.domain.events.publish.ClaimVerifiedEvent;
import uk.co.andrewrea.claim.fraud.domain.events.subscribe.ClaimRegisteredEvent;
import uk.co.andrewrea.infrastructure.inproc.Retry;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/11/16.
 */
public class ClaimFraudHttpService {
    private ClaimFraudServiceConfiguration config;
    private final HealthCheckRegistry healthChecks = new HealthCheckRegistry();
    private Service service;

    public ClaimFraudHttpService(ClaimFraudServiceConfiguration config) {
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
        channel.exchangeDeclare(this.config.claimFraudServiceExchangeName,"topic", false);

        //Create a queue and bind to the exchange
        String queueName = String.format("%s.%s",this.config.claimRegistrationServiceExchangeName, this.config.claimFraudServiceExchangeName);
        channel.queueDeclare(queueName,false, false, false, null);
        channel.queueBind(queueName, this.config.claimRegistrationServiceExchangeName, ClaimRegisteredEvent.NAME);

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
                                ClaimRegisteredEvent claimRegisteredEvent = new Gson().fromJson(new String(body), ClaimRegisteredEvent.class);
                                ClaimVerifiedEvent claimVerifiedEvent = new ClaimVerifiedEvent();
                                claimVerifiedEvent.claim = claimRegisteredEvent.claim;

                                byte[] messageBodyBytes = new Gson().toJson(claimVerifiedEvent).getBytes();

                                AMQP.BasicProperties messageProperties = new AMQP.BasicProperties.Builder()
                                        .contentType("application/json")
                                        .build();

                                channel.basicPublish(config.claimFraudServiceExchangeName, ClaimVerifiedEvent.NAME, messageProperties, messageBodyBytes);

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

    public void stop(){
        this.service.stop();
    }
}
