import com.rabbitmq.client.Channel;
import spark.Service;
import uk.co.andrewrea.claim.fraud.config.ClaimFraudServiceConfiguration;
import uk.co.andrewrea.claim.fraud.domain.dtos.AddressDto;
import uk.co.andrewrea.claim.fraud.domain.dtos.BankAccountDto;
import uk.co.andrewrea.claim.fraud.domain.dtos.ClaimDto;
import uk.co.andrewrea.claim.fraud.services.ClaimFraudHttpService;
import uk.co.andrewrea.infrastructure.core.Publisher;
import uk.co.andrewrea.infrastructure.rabbitmq.RabbitMQPublisher;
import uk.co.andrewrea.infrastructure.rabbitmq.test.RabbitMQFacadeForTest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/9/16.
 */
public class SystemUnderTest {

    private RabbitMQFacadeForTest rabbitMqFacade;
    private ClaimFraudServiceConfiguration configuration;

    public SystemUnderTest(RabbitMQFacadeForTest rabbitMqFacade){

        this.rabbitMqFacade = rabbitMqFacade;
        this.configuration = new ClaimFraudServiceConfiguration();
    }

    public ClaimDto getSampleClaim() {
        ClaimDto claim = new ClaimDto();
        claim.firstname = "John";
        claim.middlenames = "Jospeh";
        claim.surname = "Doe";
        claim.dob = "1983/04/21";
        claim.nino = "AB000000A";
        claim.income = 21000;
        claim.passportNumber = "123456789";

        BankAccountDto bankAccount = new BankAccountDto();
        bankAccount.name = "knowles and barclays";
        bankAccount.number = "87654321";
        bankAccount.sortCode = "00-00-00";
        claim.bankAccount = bankAccount;

        AddressDto address = new AddressDto();
        address.line1 = "10 Some Street";
        address.line2 = "Some Place";
        address.town = "Some Town";
        address.city = "Some City";
        address.postCode = "XX1 1XX";
        claim.address = address;

        claim.email = "john@not.exists";
        claim.receiveEmail = true;

        return claim;
    }

    public ClaimFraudServiceConfiguration getConfiguration() {
        return configuration;
    }


    public ClaimFraudHttpService createClaimFraudService() throws IOException, TimeoutException {

        ClaimFraudServiceConfiguration fraudServiceConfiguration = new ClaimFraudServiceConfiguration();
        this.rabbitMqFacade.setupTopicExchangeFor(fraudServiceConfiguration.claimFraudServiceExchangeName);
        Channel publisherChannel = this.rabbitMqFacade.createLocalRabbitMQChannel();
        Publisher publisher = new RabbitMQPublisher(publisherChannel, fraudServiceConfiguration.claimFraudServiceExchangeName);
        Service server = Service.ignite().port(fraudServiceConfiguration.port);
        ClaimFraudHttpService claimFraudService = new ClaimFraudHttpService(server, publisher, fraudServiceConfiguration);
        return claimFraudService;
    }
}
