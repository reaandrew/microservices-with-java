package uk.co.andrewrea.claim.fraud.services;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import spark.Service;
import uk.co.andrewrea.claim.fraud.config.ClaimFraudServiceConfiguration;
import uk.co.andrewrea.claim.fraud.domain.events.publish.ClaimVerifiedEvent;
import uk.co.andrewrea.claim.fraud.domain.events.subscribe.ClaimRegisteredEvent;
import uk.co.andrewrea.infrastructure.core.Publisher;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/11/16.
 */
public class ClaimFraudHttpService {
    private final Service service;
    private final Publisher publisher;
    private ClaimFraudServiceConfiguration config;

    public ClaimFraudHttpService(Service service, Publisher publisher, ClaimFraudServiceConfiguration config) {

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
        String queueName = String.format("%s.%s",this.config.claimRegistrationServiceExchangeName, this.config.claimFraudServiceExchangeName);
        channel.queueDeclare(queueName,false, true, true, null);
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
