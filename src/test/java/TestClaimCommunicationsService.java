import org.junit.*;
import uk.co.andrewrea.domain.core.communication.CommunicationService;
import uk.co.andrewrea.domain.dtos.ClaimDto;
import uk.co.andrewrea.domain.events.ClaimAwardPaidEvent;
import uk.co.andrewrea.domain.events.ClaimAwardedEvent;
import uk.co.andrewrea.domain.events.ClaimRegisteredEvent;
import uk.co.andrewrea.infrastructure.domain.communication.inproc.InProcCommunicationService;
import uk.co.andrewrea.services.ClaimCommunicationHttpService;
import uk.co.andrewrea.services.ClaimPaymentHttpService;
import uk.co.andrewrea.services.ClaimRegistrationHttpService;
import utils.StubEmailService;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/21/16.
 */
public class TestClaimCommunicationsService {

    private SystemUnderTest sut = new SystemUnderTest();

    @Before
    public void before() throws IOException, TimeoutException {
        this.sut.startRabbitMQSystem();
    }

    @After
    public void after() throws IOException, TimeoutException {
        this.sut.stopRabbitMQSystem();
    }

    @Test
    public void sendsClaimantEmail() throws IOException, TimeoutException, InterruptedException {


        //Setup exchange for the ClaimRegistrationService
        this.sut.setupExchangeFor(ClaimRegistrationHttpService.NAME);

        //Setup exchange for the ClaimPaymentService
        this.sut.setupExchangeFor(ClaimPaymentHttpService.NAME);

        //Create a STUB Email API Service
        StubEmailService stubEmailService = new StubEmailService();

        //Create a ClaimService which currently will be the InProc one
        InProcCommunicationService communicationService = new InProcCommunicationService(stubEmailService);

        //Create the Claim Communication Service
        ClaimCommunicationHttpService claimCommunicationHttpService = new ClaimCommunicationHttpService(communicationService);
        claimCommunicationHttpService.start();

        //Publish a ClaimRegisteredEvent for the communications service to know
        //  how the claimant would like to be contacted.
        // - Setting Email = true

        ClaimDto claim = this.sut.getSampleClaim();
        String claimId = "someClaimId";

        ClaimRegisteredEvent claimRegisteredEvent = new ClaimRegisteredEvent();
        claimRegisteredEvent.id = claimId;
        claimRegisteredEvent.claim = claim;

        this.sut.publishTo(ClaimRegistrationHttpService.NAME, ClaimRegisteredEvent.NAME, claimRegisteredEvent);

        //Publish a ClaimAwardPaidEvent
        ClaimAwardPaidEvent claimAwardPaidEvent = new ClaimAwardPaidEvent();
        claimAwardPaidEvent.id = claimId;

        this.sut.publishTo(ClaimPaymentHttpService.NAME, ClaimAwardPaidEvent.NAME, claimAwardPaidEvent);

        Thread.sleep(100);
        //Expect the Email API Service is invoked
        Assert.assertTrue(stubEmailService.emailSent(claim.email,"Claim has been sent"));

    }
}
