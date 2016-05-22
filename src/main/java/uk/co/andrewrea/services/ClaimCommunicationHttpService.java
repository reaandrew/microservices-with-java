package uk.co.andrewrea.services;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import uk.co.andrewrea.core.EmailService;
import uk.co.andrewrea.domain.core.communication.CommunicationService;
import uk.co.andrewrea.domain.events.ClaimAwardPaidEvent;
import uk.co.andrewrea.domain.events.ClaimRegisteredEvent;
import uk.co.andrewrea.domain.models.communication.Communication;
import uk.co.andrewrea.infrastructure.rabbitmq.ConsumingServiceQueueName;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/21/16.
 */
public class ClaimCommunicationHttpService {
    private static final String NAME = "claim-communication-http=service";
    private CommunicationService communicationService;

    public ClaimCommunicationHttpService(CommunicationService communicationService){

        this.communicationService = communicationService;
    }

    public void start() throws IOException, TimeoutException {
//Create a connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setVirtualHost("/");
        factory.setHost("localhost");
        factory.setPort(5672);
        Connection conn = factory.newConnection();
        Channel channel = conn.createChannel();

        runRegistrationConsumer(channel);
        runPaymentConsumer(channel);
    }

    private void runRegistrationConsumer(final Channel channel) throws IOException {
        //Create a queue and bind to the exchange for PAYMENT
        String queueName = new ConsumingServiceQueueName(ClaimRegistrationHttpService.NAME, ClaimCommunicationHttpService.NAME).toString();
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
    }

    private void runPaymentConsumer(final Channel channel) throws IOException {
        //Create a queue and bind to the exchange for PAYMENT
        String queueName = new ConsumingServiceQueueName(ClaimPaymentHttpService.NAME, ClaimCommunicationHttpService.NAME).toString();
        channel.queueDeclare(queueName,false, true, true, null);
        channel.queueBind(queueName, ClaimPaymentHttpService.NAME, ClaimAwardPaidEvent.NAME);

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
    }
}
