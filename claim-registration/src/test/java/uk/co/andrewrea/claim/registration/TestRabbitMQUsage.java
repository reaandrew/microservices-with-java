package uk.co.andrewrea.claim.registration;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.rabbitmq.client.*;
import org.json.JSONObject;
import org.junit.*;
import spark.Service;
import uk.co.andrewrea.infrastructure.core.Publisher;
import uk.co.andrewrea.infrastructure.rabbitmq.RabbitMQPublisher;
import uk.co.andrewrea.infrastructure.rabbitmq.test.RabbitMQFacadeForTest;
import uk.co.andrewrea.registration.config.ClaimSubmissionConfiguration;
import uk.co.andrewrea.registration.domain.dtos.ClaimDto;
import uk.co.andrewrea.registration.domain.events.publish.ClaimSubmittedEvent;
import uk.co.andrewrea.registration.services.ClaimSubmissionHttpService;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/9/16.
 */
public class TestRabbitMQUsage {


    private SystemUnderTest sut;
    private RabbitMQFacadeForTest rabbitMQFacadeForTest;

    @Before
    public void before() throws IOException, TimeoutException {
        this.rabbitMQFacadeForTest = new RabbitMQFacadeForTest();
        this.sut = new SystemUnderTest(this.rabbitMQFacadeForTest);
        this.rabbitMQFacadeForTest.startRabbitMQSystem();
    }

    @After
    public void after() throws IOException, TimeoutException {
        this.rabbitMQFacadeForTest.stopRabbitMQSystem();
    }

    @Test
    public void canDoSomething() throws IOException, TimeoutException, UnirestException, InterruptedException {

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
        ClaimDto body = this.sut.getSampleClaim();

        Thread.sleep(10);
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
