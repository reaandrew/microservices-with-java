import com.mashape.unirest.http.exceptions.UnirestException;
import com.rabbitmq.client.Channel;
import org.junit.Test;
import spark.Service;
import uk.co.andrewrea.config.ClaimFraudServiceConfiguration;
import uk.co.andrewrea.domain.dtos.ClaimDto;
import uk.co.andrewrea.domain.events.ClaimRegisteredEvent;
import uk.co.andrewrea.domain.events.ClaimVerifiedEvent;
import uk.co.andrewrea.events.Publisher;
import uk.co.andrewrea.infrastructure.rabbitmq.RabbitMQPublisher;
import uk.co.andrewrea.services.ClaimFraudHttpService;
import uk.co.andrewrea.services.ClaimRegistrationHttpService;
import utils.RabbitMQExpections;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/12/16.
 */
public class TestClaimFraudHttpService {

    private SystemUnderTest sut = new SystemUnderTest();

    @Test
    public void publishesClaimVerifiedEvent() throws IOException, TimeoutException, UnirestException, InterruptedException {

        this.sut.startRabbitMQSystem();
        this.sut.setupExchangeFor(ClaimRegistrationHttpService.NAME);

        //ARRANGE
        ClaimFraudHttpService claimFraudService = this.sut.createClaimFraudService();
        claimFraudService.start();

        RabbitMQExpections rabbitMQExpectations = new RabbitMQExpections(sut.createLocalRabbitMQChannel());

        rabbitMQExpectations.ExpectForExchange(ClaimFraudHttpService.NAME, messages -> {
            return messages.stream().filter(rabbitMQMessage ->
                    rabbitMQMessage.envelope.getRoutingKey().equals(ClaimVerifiedEvent.NAME)).count() == 1;
        });

        ClaimDto claim = sut.getSampleClaim();

        //ACT
        ClaimRegisteredEvent claimRegisteredEvent = new ClaimRegisteredEvent();
        claimRegisteredEvent.dob = claim.dob;
        claimRegisteredEvent.id = "someClaimId";
        claimRegisteredEvent.income = claim.income;
        claimRegisteredEvent.nino = claim.nino;
        claimRegisteredEvent.surname = claim.surname;
        claimRegisteredEvent.middlenames = claim.middlenames;
        claimRegisteredEvent.firstname = claim.firstname;
        claimRegisteredEvent.address = claim.address;
        claimRegisteredEvent.bankAccount = claim.bankAccount;
        claimRegisteredEvent.passportNumber = claim.passportNumber;

        this.sut.publishTo(ClaimRegistrationHttpService.NAME, ClaimRegisteredEvent.NAME, claimRegisteredEvent);


        //ASSERT
        try {
            rabbitMQExpectations.VerifyAllExpectations();
        }finally{
            claimFraudService.stop();
            this.sut.stopRabbitMQSystem();
        }

    }
}
