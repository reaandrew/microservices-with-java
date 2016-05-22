import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rabbitmq.test.RabbitMQExpections;
import rabbitmq.test.RabbitMQFacadeForTest;
import uk.co.andrewrea.registration.config.ClaimFraudServiceConfiguration;
import uk.co.andrewrea.registration.domain.dtos.ClaimDto;
import uk.co.andrewrea.registration.domain.events.ClaimRegisteredEvent;
import uk.co.andrewrea.registration.domain.events.ClaimVerifiedEvent;
import uk.co.andrewrea.registration.services.ClaimFraudHttpService;
import uk.co.andrewrea.registration.services.ClaimRegistrationHttpService;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class TestClaimFraudHttpService {
    private SystemUnderTest sut;
    private RabbitMQFacadeForTest rabbitMQFacadeForTest;
    private ClaimFraudServiceConfiguration config;

    @Before
    public void before() throws IOException, TimeoutException {
        this.rabbitMQFacadeForTest = new RabbitMQFacadeForTest();
        this.sut = new SystemUnderTest(this.rabbitMQFacadeForTest);
        this.rabbitMQFacadeForTest.startRabbitMQSystem();
        this.config = new ClaimFraudServiceConfiguration();
    }

    @After
    public void after() throws IOException, TimeoutException {
        this.rabbitMQFacadeForTest.stopRabbitMQSystem();
    }

    @Test
    public void publishesClaimVerifiedEvent() throws IOException, TimeoutException, UnirestException, InterruptedException {

        this.rabbitMQFacadeForTest.setupTopicExchangeFor(this.config.claimRegistrationServiceExchangeName);

        //ARRANGE
        ClaimFraudHttpService claimFraudService = this.sut.createClaimFraudService();
        claimFraudService.start();

        RabbitMQExpections rabbitMQExpectations = new RabbitMQExpections(this.rabbitMQFacadeForTest.createLocalRabbitMQChannel());

        rabbitMQExpectations.ExpectForExchange(this.config.claimFraudServiceExchangeName, messages -> messages.stream().filter(rabbitMQMessage ->
                rabbitMQMessage.envelope.getRoutingKey().equals(ClaimVerifiedEvent.NAME)).count() == 1);

        ClaimDto claim = sut.getSampleClaim();

        //ACT
        ClaimRegisteredEvent claimRegisteredEvent = new ClaimRegisteredEvent();
        claimRegisteredEvent.id = "someClaimId";
        claimRegisteredEvent.claim = claim;

        this.rabbitMQFacadeForTest.publishAsJson(this.config.claimRegistrationServiceExchangeName, ClaimRegisteredEvent.NAME, claimRegisteredEvent);


        //ASSERT
        try {
            rabbitMQExpectations.VerifyAllExpectations();
        }finally{
            claimFraudService.stop();
        }

    }
}
