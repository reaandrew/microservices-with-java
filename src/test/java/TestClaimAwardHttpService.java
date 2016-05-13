import com.rabbitmq.client.Channel;
import org.junit.Test;
import spark.Service;
import uk.co.andrewrea.config.ClaimAwardServiceConfiguration;
import uk.co.andrewrea.domain.dtos.ClaimDto;
import uk.co.andrewrea.domain.events.ClaimAwardedEvent;
import uk.co.andrewrea.domain.events.ClaimVerifiedEvent;
import uk.co.andrewrea.events.Publisher;
import uk.co.andrewrea.infrastructure.rabbitmq.RabbitMQPublisher;
import uk.co.andrewrea.services.ClaimAwardedHttpService;
import uk.co.andrewrea.services.ClaimFraudHttpService;
import utils.RabbitMQExpections;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/13/16.
 */
public class TestClaimAwardHttpService {

    private SystemUnderTest sut = new SystemUnderTest();

    @Test
    public void publishesClaimAwardedEvent() throws IOException, TimeoutException, InterruptedException {

        //ARRANGE
        this.sut.startRabbitMQSystem();
        this.sut.setupExchangeFor(ClaimFraudHttpService.NAME);

        ClaimAwardServiceConfiguration claimAwardServiceConfiguration = new ClaimAwardServiceConfiguration();
        Service server = Service.ignite().port(claimAwardServiceConfiguration.port);
        Channel channel = this.sut.createLocalRabbitMQChannel();
        Publisher publisher = RabbitMQPublisher.create(channel, ClaimAwardedHttpService.NAME);
        ClaimAwardedHttpService claimAwardedHttpService = new ClaimAwardedHttpService(server, publisher);
        claimAwardedHttpService.start();

        Channel expectationsChannel = this.sut.createLocalRabbitMQChannel();
        RabbitMQExpections expectations = new RabbitMQExpections(expectationsChannel);
        expectations.ExpectForExchange(ClaimAwardedHttpService.NAME, messages -> {
            return messages.size() == 1 && messages.get(0).envelope.getRoutingKey().equals(ClaimAwardedEvent.NAME);
        });

        ClaimDto claim = this.sut.getSampleClaim();
        ClaimVerifiedEvent claimVerifiedEvent = new ClaimVerifiedEvent();
        claimVerifiedEvent.id = "somseId";
        claimVerifiedEvent.firstname = claim.firstname;
        claimVerifiedEvent.middlenames = claim.middlenames;
        claimVerifiedEvent.surname = claim.surname;
        claimVerifiedEvent.dob = claim.dob;
        claimVerifiedEvent.nino = claim.nino;
        claimVerifiedEvent.income = claim.income;
        claimVerifiedEvent.passportNumber = claim.passportNumber;
        claimVerifiedEvent.bankAccount = claim.bankAccount;
        claimVerifiedEvent.address = claim.address;

        //ACT
        this.sut.publishTo(ClaimFraudHttpService.NAME, ClaimVerifiedEvent.NAME, claimVerifiedEvent);

        //ASSERT
        try {
            expectations.VerifyAllExpectations();
        } finally {
            claimAwardedHttpService.stop();
            this.sut.stopRabbitMQSystem();
        }
    }
}
