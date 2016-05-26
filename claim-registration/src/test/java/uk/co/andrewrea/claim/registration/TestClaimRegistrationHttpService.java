package uk.co.andrewrea.claim.registration;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.rabbitmq.client.Channel;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import spark.Service;
import uk.co.andrewrea.claim.registration.config.ClaimRegistrationConfiguration;
import uk.co.andrewrea.claim.registration.domain.events.publish.ClaimRegisteredEvent;
import uk.co.andrewrea.infrastructure.core.Publisher;
import uk.co.andrewrea.infrastructure.rabbitmq.RabbitMQPublisher;
import uk.co.andrewrea.infrastructure.rabbitmq.test.RabbitMQExpections;
import uk.co.andrewrea.infrastructure.rabbitmq.test.RabbitMQFacadeForTest;
import uk.co.andrewrea.claim.registration.domain.dtos.ClaimDto;
import uk.co.andrewrea.claim.registration.services.ClaimRegistrationHttpService;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/11/16.
 */
public class TestClaimRegistrationHttpService {

    private ClaimRegistrationConfiguration config;

    private SystemUnderTest sut;
    private RabbitMQFacadeForTest rabbitMQFacadeForTest;

    @Before
    public void before() throws IOException, TimeoutException {
        this.rabbitMQFacadeForTest = new RabbitMQFacadeForTest();
        this.sut = new SystemUnderTest();
        this.rabbitMQFacadeForTest.startRabbitMQSystem();
        this.config = new ClaimRegistrationConfiguration();
        this.config.amqpUsername = "admin";
        this.config.amqpPassword = "admin";
    }

    @After
    public void after() throws IOException, TimeoutException {
        this.rabbitMQFacadeForTest.stopRabbitMQSystem();
    }

    @Test
    public void publishesClaimRegisteredEvent() throws IOException, UnirestException, TimeoutException, InterruptedException {

        //TODO: Should not need this as the setup of the service should deal with this.
        this.rabbitMQFacadeForTest.setupTopicExchangeFor(this.config.claimRegistrationServiceExchangeName);

        ClaimRegistrationHttpService service = new ClaimRegistrationHttpService(this.config);
        service.start();

        RabbitMQExpections rabbitMQExpectations = new RabbitMQExpections(this.rabbitMQFacadeForTest.createLocalRabbitMQChannel());
        rabbitMQExpectations.ExpectForExchange(this.config.claimRegistrationServiceExchangeName, messages -> {
            return messages.size() == 1 && messages.get(0).envelope.getRoutingKey().equals(ClaimRegisteredEvent.NAME);
        });

        ClaimDto claim = sut.getSampleClaim();

        HttpResponse<String> response = Unirest.post(String.format("http://localhost:%d/claims", config.servicePort))
                .body(new JSONObject(claim).toString())
                .asString();

        Assert.assertEquals(202, response.getCode());


        try {
            rabbitMQExpectations.VerifyAllExpectations();
        }
        finally{
            service.stop();
        }

    }
}
