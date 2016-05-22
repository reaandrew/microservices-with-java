package uk.co.andrewrea.claim.award.services;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import spark.Service;
import uk.co.andrewrea.claim.award.config.ClaimAwardServiceConfiguration;
import uk.co.andrewrea.claim.award.domain.events.publish.ClaimAwardedEvent;
import uk.co.andrewrea.claim.award.domain.events.subscribe.ClaimVerifiedEvent;
import uk.co.andrewrea.infrastructure.core.Publisher;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/13/16.
 */
public class ClaimAwardedHttpService {
    private Service service;
    private ClaimAwardServiceConfiguration config;

    public ClaimAwardedHttpService(ClaimAwardServiceConfiguration config) {
        this.config = config;
    }

    public void start() throws IOException, TimeoutException {
        this.service = Service.ignite().port(config.servicePort).ipAddress(config.serviceIp);

        //Create a connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setVirtualHost("/");
        factory.setHost(this.config.amqpHost);
        factory.setPort(this.config.amqpPort);
        factory.setUsername("admin");
        factory.setPassword("admin");
        Connection conn = factory.newConnection();
        Channel channel = conn.createChannel();

        //Create a queue and bind to the exchange
        String queueName = String.format("%s.%s",this.config.claimFraudServiceExchangeName, this.config.claimAwardServiceExchangeName);
        channel.queueDeclare(queueName,false, true, true, null);
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
