import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.rabbitmq.client.Channel;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rabbitmq.test.RabbitMQExpections;
import rabbitmq.test.RabbitMQFacadeForTest;
import spark.Service;
import uk.co.andrewrea.infrastructure.events.Publisher;
import uk.co.andrewrea.infrastructure.rabbitmq.RabbitMQPublisher;
import uk.co.andrewrea.registration.config.ClaimConfiguration;
import uk.co.andrewrea.registration.config.ClaimRegistrationConfiguration;
import uk.co.andrewrea.registration.domain.dtos.ClaimDto;
import uk.co.andrewrea.registration.domain.events.ClaimRegisteredEvent;
import uk.co.andrewrea.registration.services.ClaimRegistrationHttpService;
import utils.TestIdGenerator;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/11/16.
 */
public class TestClaimRegistrationHttpService {
    private TestIdGenerator idGenerator = new TestIdGenerator();

    private ClaimConfiguration claimServiceConfiguration;
    private ClaimRegistrationHttpService service;
    private ClaimRegistrationConfiguration config;

    private SystemUnderTest sut;
    private RabbitMQFacadeForTest rabbitMQFacadeForTest;

    @Before
    public void before() throws IOException, TimeoutException {
        this.rabbitMQFacadeForTest = new RabbitMQFacadeForTest();
        this.sut = new SystemUnderTest(this.rabbitMQFacadeForTest);
        this.rabbitMQFacadeForTest.startRabbitMQSystem();
        this.config = new ClaimRegistrationConfiguration();
    }

    @After
    public void after() throws IOException, TimeoutException {
        this.rabbitMQFacadeForTest.stopRabbitMQSystem();
    }

    @Test
    public void publishesClaimRegisteredEvent() throws IOException, UnirestException, TimeoutException, InterruptedException {

        //TODO: Should not need this as the setup of the service should deal with this.
        this.rabbitMQFacadeForTest.setupTopicExchangeFor(this.config.claimRegistrationServiceExchangeName);

        this.claimServiceConfiguration = new ClaimConfiguration();

        Channel channel = this.rabbitMQFacadeForTest.createLocalRabbitMQChannel();
        Publisher publisher = new RabbitMQPublisher(channel, this.config.claimRegistrationServiceExchangeName);

        Service http = Service.ignite().port(this.claimServiceConfiguration.port);
        this.service = new ClaimRegistrationHttpService(this.idGenerator, http, publisher);
        this.service.start();

        RabbitMQExpections rabbitMQExpectations = new RabbitMQExpections(this.rabbitMQFacadeForTest.createLocalRabbitMQChannel());
        rabbitMQExpectations.ExpectForExchange(this.config.claimRegistrationServiceExchangeName, messages -> {
            return messages.size() == 1 && messages.get(0).envelope.getRoutingKey().equals(ClaimRegisteredEvent.NAME);
        });

        ClaimDto claim = sut.getSampleClaim();

        HttpResponse<String> response = Unirest.post(String.format("http://localhost:%d/claims", this.claimServiceConfiguration.port))
                .body(new JSONObject(claim).toString())
                .asString();

        Assert.assertEquals(202, response.getCode());


        try {
            rabbitMQExpectations.VerifyAllExpectations();
        }
        finally{
            this.service.stop();
        }

    }
}
