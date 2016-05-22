import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.rabbitmq.client.*;
import org.json.JSONObject;
import org.junit.*;
import spark.Service;
import uk.co.andrewrea.infrastructure.core.Publisher;
import uk.co.andrewrea.infrastructure.rabbitmq.RabbitMQPublisher;
import uk.co.andrewrea.infrastructure.rabbitmq.test.RabbitMQFacadeForTest;
import uk.co.andrewrea.registration.config.ClaimRegistrationConfiguration;
import uk.co.andrewrea.registration.config.ClaimSubmissionConfiguration;
import uk.co.andrewrea.registration.domain.dtos.ClaimDto;
import uk.co.andrewrea.registration.domain.events.ClaimRegisteredEvent;
import uk.co.andrewrea.registration.services.OldClaimRegistrationHttpService;
import uk.co.andrewrea.registration.services.ClaimSubmissionHttpService;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/10/16.
 */
public class TestOldClaimRegistrationHttpService {


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
    public void subscribeTo_ClaimSubmittedEvent_andPublish_ClaimRegisteredEvent() throws IOException, TimeoutException, UnirestException {

        final CountDownLatch signal = new CountDownLatch(1);

        ClaimSubmissionConfiguration claimSubmissionConfiguration = new ClaimSubmissionConfiguration();
        ClaimRegistrationConfiguration claimRegistrationConfiguration = new ClaimRegistrationConfiguration();

        //Create a connection
        ConnectionFactory factory = new ConnectionFactory();
        factory.setVirtualHost("/");
        factory.setHost("localhost");
        factory.setPort(5672);
        Connection conn = factory.newConnection();
        Channel channel = conn.createChannel();

        //Create an exchange
        channel.exchangeDeclare(ClaimSubmissionHttpService.NAME, "topic", false);
        channel.exchangeDeclare(OldClaimRegistrationHttpService.NAME, "topic", false);

        //Create a rabbitMQ publisher

        Publisher claimSubmissionPublisher = new RabbitMQPublisher(channel, ClaimSubmissionHttpService.NAME);

        //Create a ClaimSubmissionHttpService
        Service claimSubmissionHttp = Service.ignite().port(claimSubmissionConfiguration.port);
        ClaimSubmissionHttpService claimSubmissionHttpService = new ClaimSubmissionHttpService(claimSubmissionHttp, claimSubmissionPublisher);
        claimSubmissionHttpService.start();

        //Create a channel
        Channel registrationChannel = conn.createChannel();

        //Create an exchange
        channel.exchangeDeclare(ClaimSubmissionHttpService.NAME, "topic", false);

        //Create a rabbitMQ publisher

        Publisher claimRegistrationPublisher = new RabbitMQPublisher(registrationChannel, OldClaimRegistrationHttpService.NAME);

        //Create a OldClaimRegistrationHttpService
        Service claimRegisteredHttp = Service.ignite().port(claimRegistrationConfiguration.port);
        OldClaimRegistrationHttpService oldClaimRegistrationHttpService = new OldClaimRegistrationHttpService(claimRegisteredHttp, claimRegistrationPublisher);
        oldClaimRegistrationHttpService.start();

        //Create a queue and bind to the exchange
        String testQueueName = "TestOldClaimRegistrationHttpService";
        channel.queueDeclare(testQueueName,false, true, true, null);
        channel.queueBind(testQueueName,OldClaimRegistrationHttpService.NAME, ClaimRegisteredEvent.NAME);

        //Create a consumer of the queue
        Runnable consumer = () -> {
            boolean autoAck = false;
            try {
                channel.basicConsume(testQueueName, autoAck,
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

        ClaimDto body = this.sut.getSampleClaim();

        Unirest.post("http://localhost:8080/claims")
                .body(new JSONObject(body).toString())
                .asString();

        //Expect a ClaimRegisteredEvent was published
        try {
            boolean triggered = signal.await(5, TimeUnit.SECONDS);
            if (!triggered){
                Assert.fail("Signal was not triggered");
            }
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }

        oldClaimRegistrationHttpService.stop();
        claimSubmissionHttpService.stop();
        registrationChannel.close();
        channel.close();
        conn.close();
    }

}
