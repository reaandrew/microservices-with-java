package uk.co.andrewrea.services;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import spark.Service;
import uk.co.andrewrea.domain.events.ClaimRegisteredEvent;
import uk.co.andrewrea.domain.events.ClaimVerifiedEvent;
import uk.co.andrewrea.events.Publisher;
import uk.co.andrewrea.infrastructure.rabbitmq.ConsumingServiceQueueName;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/11/16.
 */
public class ClaimFraudHttpService {
    public static final String NAME = "ClaimFraudHttpService";
    private final Service service;
    private final Publisher publisher;

    public ClaimFraudHttpService(Service service, Publisher publisher) {

        this.service = service;
        this.publisher = publisher;
    }

    public void start() throws IOException, TimeoutException {

        //Create a connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setVirtualHost("/");
        factory.setHost("localhost");
        factory.setPort(5672);
        Connection conn = factory.newConnection();
        Channel channel = conn.createChannel();

        //Create a queue and bind to the exchange
        String queueName = new ConsumingServiceQueueName(ClaimRegistrationHttpService.NAME, ClaimFraudHttpService.NAME).toString();
        channel.queueDeclare(queueName,false, true, true, null);
        channel.queueBind(queueName, ClaimRegistrationHttpService.NAME, ClaimRegisteredEvent.NAME);

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
                                ClaimRegisteredEvent claimRegisteredEvent = new Gson().fromJson(new String(body), ClaimRegisteredEvent.class);
                                ClaimVerifiedEvent claimVerifiedEvent = new ClaimVerifiedEvent();
                                claimVerifiedEvent.claim = claimRegisteredEvent.claim;

                                publisher.publish(claimVerifiedEvent, ClaimVerifiedEvent.NAME);

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
