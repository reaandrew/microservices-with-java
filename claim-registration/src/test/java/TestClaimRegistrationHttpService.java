import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.andrewrea.claim.registration.config.ClaimRegistrationConfiguration;
import uk.co.andrewrea.claim.registration.domain.events.publish.ClaimRegisteredEvent;
import uk.co.andrewrea.infrastructure.rabbitmq.test.RabbitMQExpections;
import uk.co.andrewrea.infrastructure.rabbitmq.test.RabbitMQFacadeForTest;
import uk.co.andrewrea.claim.registration.domain.dtos.ClaimDto;
import uk.co.andrewrea.claim.registration.services.ClaimRegistrationHttpService;
import uk.co.andrewrea.infrastructure.spark.Settings;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/11/16.
 */
public class TestClaimRegistrationHttpService {

    private ClaimRegistrationConfiguration config;

    private SystemUnderTest sut;
    private RabbitMQFacadeForTest rabbitMQFacadeForTest;
    private InMemoryClaimService claimService;

    @Before
    public void before() throws IOException, TimeoutException {
        this.rabbitMQFacadeForTest = new RabbitMQFacadeForTest();
        this.sut = new SystemUnderTest();
        this.rabbitMQFacadeForTest.startRabbitMQSystem();
        this.config = new ClaimRegistrationConfiguration();
        this.config.amqpHost = "localhost";
        this.config.amqpUsername = "admin";
        this.config.amqpPassword = "admin";

        this.claimService = new InMemoryClaimService();
    }

    @After
    public void after() throws IOException, TimeoutException {
        this.rabbitMQFacadeForTest.stopRabbitMQSystem();
    }

    @Test
    public void publishesClaimRegisteredEvent() throws IOException, UnirestException, TimeoutException, InterruptedException {

        //TODO: Should not need this as the setup of the service should deal with this.
        this.rabbitMQFacadeForTest.setupTopicExchangeFor(this.config.claimRegistrationServiceExchangeName);

        ClaimRegistrationHttpService service = new ClaimRegistrationHttpService(this.config, this.claimService);
        service.start();

        Thread.sleep(Settings.SERVER_INIT_WAIT);

        RabbitMQExpections rabbitMQExpectations = new RabbitMQExpections(this.rabbitMQFacadeForTest.createLocalRabbitMQChannel());
        rabbitMQExpectations.ExpectForExchange(this.config.claimRegistrationServiceExchangeName, messages -> {
            return messages.size() == 1 && messages.get(0).envelope.getRoutingKey().equals(ClaimRegisteredEvent.NAME);
        });

        ClaimDto claim = sut.getSampleClaim();

        String jsonBody = new Gson().toJson(claim).toString();

        HttpResponse<String> response = Unirest.post(String.format("http://localhost:%d/claims", config.servicePort))
                .body(jsonBody)
                .asString();

        Assert.assertEquals(202, response.getCode());
        Assert.assertEquals(1, claimService.getNumberOfClaimsSubmitted());

        try {
            rabbitMQExpectations.VerifyAllExpectations();
        } finally {
            service.stop();
        }

    }
}
