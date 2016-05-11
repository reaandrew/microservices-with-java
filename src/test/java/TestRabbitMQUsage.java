import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.rabbitmq.client.*;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import spark.Service;
import uk.co.andrewrea.config.ClaimSubmissionConfiguration;
import uk.co.andrewrea.domain.events.ClaimSubmittedEvent;
import uk.co.andrewrea.services.ClaimSubmissionHttpService;
import uk.co.andrewrea.events.Publisher;
import uk.co.andrewrea.infrastructure.rabbitmq.RabbitMQPublisher;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/9/16.
 */
public class TestRabbitMQUsage {
    @Test
    public void canDoSomething() throws IOException, TimeoutException, UnirestException {

        final CountDownLatch signal = new CountDownLatch(1);
        final String queueName = "canDoSomething";
        final String exchangeName = "sampleService";
        final String eventType = ClaimSubmittedEvent.NAME;
        final ClaimSubmissionConfiguration claimSubmissionConfiguration = new ClaimSubmissionConfiguration();

        //Create a connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setVirtualHost("/");
        factory.setHost("localhost");
        factory.setPort(5672);
        Connection conn = factory.newConnection();
        Channel channel = conn.createChannel();

        //Create an exchange
        channel.exchangeDeclare(exchangeName,"topic",false);

        //Create a rabbitMQ publisher

        Publisher rmqPublisher = new RabbitMQPublisher(channel, exchangeName);

        //Create a service instance with the rabbitMQ publisher
        Service http = Service.ignite().port(claimSubmissionConfiguration.port);
        ClaimSubmissionHttpService claimSubsmissionService = new ClaimSubmissionHttpService(http, rmqPublisher);
        claimSubsmissionService.start();

        //Create a queue and bind to the exchange
        channel.queueDeclare(queueName,false, true, true, null);
        channel.queueBind(queueName,exchangeName,eventType);

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
                                //String routingKey = envelope.getRoutingKey();
                                //String contentType = properties.getContentType();
                                long deliveryTag = envelope.getDeliveryTag();
                                channel.basicAck(deliveryTag, false);
                                signal.countDown();
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        consumer.run();

        //Submit a new claim
        HashMap body = new SystemUnderTest().getSampleClaim();

        Unirest.post(String.format("http://localhost:%d/claims",claimSubmissionConfiguration.port))
                .body(new JSONObject(body).toString())
                .asString();

        //Expect a ClaimSubmittedEvent was published
        try {
            boolean triggered = signal.await(60, TimeUnit.SECONDS);
            if (!triggered){
                Assert.fail("Signal was not triggered");
            }
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }

        claimSubsmissionService.stop();
        channel.close();
        conn.close();

    }
}
