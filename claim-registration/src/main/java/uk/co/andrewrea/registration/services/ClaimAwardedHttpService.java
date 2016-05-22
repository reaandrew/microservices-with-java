package uk.co.andrewrea.registration.services;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import spark.Service;
import uk.co.andrewrea.infrastructure.core.Publisher;
import uk.co.andrewrea.registration.config.ClaimAwardServiceConfiguration;
import uk.co.andrewrea.registration.domain.events.ClaimAwardedEvent;
import uk.co.andrewrea.registration.domain.events.ClaimVerifiedEvent;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/13/16.
 */
public class ClaimAwardedHttpService {
    private Service service;
    private Publisher publisher;
    private ClaimAwardServiceConfiguration config;

    public ClaimAwardedHttpService(Service service, Publisher publisher, ClaimAwardServiceConfiguration config) {
        this.service = service;
        this.publisher = publisher;
        this.config = config;
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
        String queueName = String.format("%s.%s",this.config.claimFraudServiceExchangeName, this.config.claimAwardServiceExchangeName);
        channel.queueDeclare(queueName,false, true, true, null);
        channel.queueBind(queueName, this.config.claimFraudServiceExchangeName, ClaimVerifiedEvent.NAME);

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
                                ClaimVerifiedEvent claimVerifiedEvent = new Gson().fromJson(new String(body), ClaimVerifiedEvent.class);

                                ClaimAwardedEvent claimAwardedEvent = new ClaimAwardedEvent();
                                claimAwardedEvent.claim = claimVerifiedEvent.claim;

                                publisher.publish(claimAwardedEvent, ClaimAwardedEvent.NAME);

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
