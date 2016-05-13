import com.rabbitmq.client.Channel;
import org.junit.Test;
import spark.Service;
import uk.co.andrewrea.config.ClaimPaymentServiceConfiguration;
import uk.co.andrewrea.domain.dtos.ClaimDto;
import uk.co.andrewrea.domain.events.ClaimAwardPaidEvent;
import uk.co.andrewrea.domain.events.ClaimAwardedEvent;
import uk.co.andrewrea.events.Publisher;
import uk.co.andrewrea.infrastructure.rabbitmq.RabbitMQPublisher;
import uk.co.andrewrea.services.ClaimAwardedHttpService;
import uk.co.andrewrea.services.ClaimPaymentHttpService;
import utils.RabbitMQExpections;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/13/16.
 */
public class TestClaimPaymentService {

    private SystemUnderTest sut = new SystemUnderTest();

    @Test
    public void publishesClaimAwardPaidEvent() throws IOException, InterruptedException, TimeoutException {

        this.sut.startRabbitMQSystem();
        this.sut.setupExchangeFor(ClaimAwardedHttpService.NAME);

        ClaimPaymentServiceConfiguration claimPaymentServiceConfiguration = new ClaimPaymentServiceConfiguration();

        Service service = Service.ignite().port(claimPaymentServiceConfiguration.port);
        Channel channel = this.sut.createLocalRabbitMQChannel();
        Publisher publisher = RabbitMQPublisher.create(channel,ClaimPaymentHttpService.NAME);
        ClaimPaymentHttpService claimPaymentHttpService = new ClaimPaymentHttpService(service, publisher);
        claimPaymentHttpService.start();

        Channel expectationsChannel = this.sut.createLocalRabbitMQChannel();
        RabbitMQExpections expectations = new RabbitMQExpections(expectationsChannel);
        expectations.ExpectForExchange(ClaimPaymentHttpService.NAME,messages -> {
           return messages.size() == 1 && messages.get(0).envelope.getRoutingKey().equals(ClaimAwardPaidEvent.NAME);
        });

        ClaimDto claim = this.sut.getSampleClaim();
        ClaimAwardedEvent claimAwardedEvent = new ClaimAwardedEvent();
        claimAwardedEvent.id = "someId";
        claimAwardedEvent.firstname = claim.firstname;
        claimAwardedEvent.middlenames = claim.middlenames;
        claimAwardedEvent.surname = claim.surname;
        claimAwardedEvent.dob = claim.dob;
        claimAwardedEvent.nino = claim.nino;
        claimAwardedEvent.income = claim.income;
        claimAwardedEvent.address = claim.address;
        claimAwardedEvent.bankAccount = claim.bankAccount;
        claimAwardedEvent.passportNumber = claim.passportNumber;

        this.sut.publishTo(ClaimAwardedHttpService.NAME, ClaimAwardedEvent.NAME, claimAwardedEvent);

        try{
            expectations.VerifyAllExpectations();
        }finally{
            claimPaymentHttpService.stop();
            this.sut.stopRabbitMQSystem();
        }
    }
}
