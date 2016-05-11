package uk.co.andrewrea.services;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.gson.Gson;
import com.rabbitmq.client.*;
import spark.Service;
import uk.co.andrewrea.domain.events.ClaimRegisteredEvent;
import uk.co.andrewrea.domain.events.ClaimSubmittedEvent;
import uk.co.andrewrea.events.Publisher;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/10/16.
 */
public class ClaimRegistrationHttpService {
    public static final String NAME = "ClaimRegistrationHttpService";
    private Service service;
    private Publisher publisher;
    private final HealthCheckRegistry healthChecks = new HealthCheckRegistry();


    public ClaimRegistrationHttpService(Service claimRegisteredHttp, Publisher publisher) {

        this.service = claimRegisteredHttp;
        this.publisher = publisher;

        healthChecks.register("application", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });
    }

    public void start(){
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
        try {
            startMessageConsumer();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    private String getQueueNameFor(String queue){
        return String.format("%s.%s",NAME,queue);
    }

    private void startMessageConsumer() throws IOException, TimeoutException {
        //Create a connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setVirtualHost("/");
        factory.setHost("localhost");
        factory.setPort(5672);
        Connection conn = factory.newConnection();
        Channel channel = conn.createChannel();

        //Create a queue and bind to the exchange
        String queueName = getQueueNameFor(ClaimSubmittedEvent.NAME);
        channel.queueDeclare(queueName,false, true, true, null);
        channel.queueBind(queueName,ClaimSubmissionHttpService.NAME, ClaimSubmittedEvent.NAME);

        //Create a consumer of the queue
        Runnable consumer = () -> {
            boolean autoAck = false;
            try {
                channel.basicConsume(queueName, autoAck,
                        new DefaultConsumer(channel) {
                            @Override
                            public void handleDelivery(String consumerTag,
                                                       Envelope envelope,
                                                       AMQP.BasicProperties properties,
                                                       byte[] body)
                                    throws IOException
                            {
                                ClaimSubmittedEvent evt = new Gson().fromJson(new String(body), ClaimSubmittedEvent.class);
                                ClaimRegisteredEvent claimRegisteredEvent = new ClaimRegisteredEvent();
                                claimRegisteredEvent.id = UUID.randomUUID().toString();
                                claimRegisteredEvent.firstname = evt.firstname;
                                claimRegisteredEvent.middlenames = evt.middlenames;
                                claimRegisteredEvent.surname = evt.surname;
                                claimRegisteredEvent.dob = evt.dob;
                                claimRegisteredEvent.nino = evt.nino;
                                claimRegisteredEvent.income = evt.income;

                                publisher.publish(claimRegisteredEvent, ClaimRegisteredEvent.NAME);

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
