package uk.co.andrewrea.claim.communication;

import org.junit.*;
import uk.co.andrewrea.claim.communication.config.ClaimCommunicationServiceConfiguration;
import uk.co.andrewrea.claim.communication.domain.dtos.ClaimDto;
import uk.co.andrewrea.claim.communication.domain.events.subscribe.ClaimAwardPaidEvent;
import uk.co.andrewrea.claim.communication.domain.events.subscribe.ClaimRegisteredEvent;
import uk.co.andrewrea.claim.communication.infrastructure.inproc.InProcCommunicationService;
import uk.co.andrewrea.claim.communication.services.ClaimCommunicationHttpService;
import uk.co.andrewrea.infrastructure.inproc.StubEmailService;
import uk.co.andrewrea.infrastructure.rabbitmq.test.RabbitMQFacadeForTest;


import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/21/16.
 */
public class TestClaimCommunicationsService {

    private SystemUnderTest sut;
    private RabbitMQFacadeForTest rabbitMQFacadeForTest;
    private ClaimCommunicationServiceConfiguration config;

    @Before
    public void before() throws IOException, TimeoutException {
        this.rabbitMQFacadeForTest = new RabbitMQFacadeForTest();
        this.sut = new SystemUnderTest();
        this.rabbitMQFacadeForTest.startRabbitMQSystem();
        this.config = new ClaimCommunicationServiceConfiguration();
    }

    @After
    public void after() throws IOException, TimeoutException {
        this.rabbitMQFacadeForTest.stopRabbitMQSystem();
    }

    @Test
    public void sendsClaimantEmail() throws IOException, TimeoutException, InterruptedException {

        //Setup exchange for the ClaimRegistrationService
        this.rabbitMQFacadeForTest.setupTopicExchangeFor(this.config.claimRegistrationServiceExchangeName);

        //Setup exchange for the ClaimPaymentService
        this.rabbitMQFacadeForTest.setupTopicExchangeFor(this.config.claimPaymentServiceExchangeName);

        //Create a STUB Email API Service
        StubEmailService stubEmailService = new StubEmailService();

        //Create a ClaimService which currently will be the InProc one
        InProcCommunicationService communicationService = new InProcCommunicationService(stubEmailService);

        ClaimCommunicationServiceConfiguration config = new ClaimCommunicationServiceConfiguration();

        //Create the Claim Communication Service
        ClaimCommunicationHttpService claimCommunicationHttpService = new ClaimCommunicationHttpService(communicationService, config);
        claimCommunicationHttpService.start();

        //Publish a ClaimRegisteredEvent for the communications service to know
        //  how the claimant would like to be contacted.
        // - Setting Email = true

        ClaimDto claim = this.sut.getSampleClaim();
        String claimId = "someClaimId";

        ClaimRegisteredEvent claimRegisteredEvent = new ClaimRegisteredEvent();
        claimRegisteredEvent.id = claimId;
        claimRegisteredEvent.claim = claim;

        this.rabbitMQFacadeForTest.publishAsJson(this.config.claimRegistrationServiceExchangeName, ClaimRegisteredEvent.NAME, claimRegisteredEvent);

        //Publish a ClaimAwardPaidEvent
        ClaimAwardPaidEvent claimAwardPaidEvent = new ClaimAwardPaidEvent();
        claimAwardPaidEvent.id = claimId;

        this.rabbitMQFacadeForTest.publishAsJson(this.config.claimPaymentServiceExchangeName, ClaimAwardPaidEvent.NAME, claimAwardPaidEvent);

        Thread.sleep(100);
        //Expect the Email API Service is invoked
        Assert.assertTrue(stubEmailService.emailSent(claim.email,"Claim has been sent"));

    }
}
