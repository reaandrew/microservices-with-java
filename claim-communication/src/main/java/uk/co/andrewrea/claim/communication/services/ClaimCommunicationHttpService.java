package uk.co.andrewrea.claim.communication.services;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.gson.Gson;
import com.rabbitmq.client.*;
import spark.Service;
import uk.co.andrewrea.claim.communication.config.ClaimCommunicationServiceConfiguration;
import uk.co.andrewrea.claim.communication.domain.core.CommunicationService;
import uk.co.andrewrea.claim.communication.domain.events.subscribe.ClaimAwardPaidEvent;
import uk.co.andrewrea.claim.communication.domain.events.subscribe.ClaimRegisteredEvent;
import uk.co.andrewrea.claim.communication.domain.models.Communication;
import uk.co.andrewrea.infrastructure.inproc.Retry;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;


public class ClaimCommunicationHttpService {
    private CommunicationService communicationService;
    private ClaimCommunicationServiceConfiguration config;
    private final HealthCheckRegistry healthChecks = new HealthCheckRegistry();
    private Service service;
    private Connection rabbitMQConnection;
    private String claimRegistrationSubscriber;
    private String claimPaymentSubscriber;

    public ClaimCommunicationHttpService(CommunicationService communicationService, ClaimCommunicationServiceConfiguration config){
        this.communicationService = communicationService;
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
        rabbitMQConnection = Retry.io(() -> {
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
        Channel channel = rabbitMQConnection.createChannel();

        //Create the host exchange
        channel.exchangeDeclare(this.config.claimCommunicationServiceExchangeName,"topic", false);

        //Create a queue and bind to the exchange for PAYMENT
        claimRegistrationSubscriber = String.format("%s.%s",this.config.claimRegistrationServiceExchangeName, this.config.claimCommunicationServiceExchangeName);
        channel.queueDeclare(claimRegistrationSubscriber,false, false, false, null);
        channel.queueBind(claimRegistrationSubscriber, this.config.claimRegistrationServiceExchangeName, ClaimRegisteredEvent.NAME);

        //Create a queue and bind to the exchange for PAYMENT
        claimPaymentSubscriber = String.format("%s.%s",this.config.claimPaymentServiceExchangeName, this.config.claimCommunicationServiceExchangeName);
        channel.queueDeclare(claimPaymentSubscriber,false, false, false, null);
        channel.queueBind(claimPaymentSubscriber, this.config.claimPaymentServiceExchangeName, ClaimAwardPaidEvent.NAME);


        runRegistrationConsumer(channel);
        runPaymentConsumer(channel);
    }

    private void runRegistrationConsumer(final Channel channel) throws IOException {


        //Create a consumer of the queue
        Runnable consumer = () -> {
            try {
                channel.basicConsume(claimRegistrationSubscriber, false,
                        new DefaultConsumer(channel) {
                            @Override
                            public void handleDelivery(String consumerTag,
                                                       Envelope envelope,
                                                       AMQP.BasicProperties properties,
                                                       byte[] body)
                                    throws IOException
                            {
                                ClaimRegisteredEvent claimRegisteredEvent = new Gson().fromJson(new String(body), ClaimRegisteredEvent.class);

                                Communication communication = new Communication(claimRegisteredEvent.id,claimRegisteredEvent.claim.email);
                                communicationService.save(communication);

                                long deliveryTag = envelope.getDeliveryTag();
                                channel.basicAck(deliveryTag, false);
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        consumer.run();
        System.out.println("registration consumer created");
    }

    private void runPaymentConsumer(final Channel channel) throws IOException {

        //Create a consumer of the queue
        Runnable consumer = () -> {
            boolean autoAck = false;
            try {
                channel.basicConsume(claimPaymentSubscriber, autoAck,
                        new DefaultConsumer(channel) {
                            @Override
                            public void handleDelivery(String consumerTag,
                                                       Envelope envelope,
                                                       AMQP.BasicProperties properties,
                                                       byte[] body)
                                    throws IOException
                            {
                                ClaimAwardPaidEvent claimAwardedEvent = new Gson().fromJson(new String(body), ClaimAwardPaidEvent.class);
                                Optional<Communication> communication = communicationService.getByClaimId(claimAwardedEvent.id);

                                if(!communication.isPresent()){
                                    throw new RuntimeException("No claim communication exists with that id");
                                }

                                communicationService.send(communication.get());

                                long deliveryTag = envelope.getDeliveryTag();
                                channel.basicAck(deliveryTag, false);
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        consumer.run();
        System.out.println("payment consumer created");
    }
}
