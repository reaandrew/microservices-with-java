import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.co.andrewrea.claim.query.core.ClaimQueryService;
import uk.co.andrewrea.claim.query.domain.dtos.ClaimDto;
import uk.co.andrewrea.claim.query.domain.events.subscribe.ClaimAwardPaidEvent;
import uk.co.andrewrea.claim.query.domain.events.subscribe.ClaimAwardedEvent;
import uk.co.andrewrea.claim.query.domain.events.subscribe.ClaimRegisteredEvent;
import uk.co.andrewrea.claim.query.domain.events.subscribe.ClaimVerifiedEvent;
import uk.co.andrewrea.claim.query.infrastructure.inproc.InMemoryClaimQueryService;
import uk.co.andrewrea.claim.query.updater.config.ClaimQueryUpdaterServiceConfiguration;
import uk.co.andrewrea.claim.query.updater.services.ClaimQueryUpdaterHttpService;
import uk.co.andrewrea.infrastructure.rabbitmq.test.RabbitMQFacadeForTest;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 6/19/16.
 */
public class TestClaimQueryUpdaterHttpService {

    private RabbitMQFacadeForTest rabbitMQFacadeForTest;
    private ClaimQueryUpdaterServiceConfiguration config;
    private ClaimQueryUpdaterHttpService service;
    private ClaimQueryService claimQueryService;

    @Before
    public void before() throws IOException, TimeoutException, InterruptedException, UnirestException, JSONException {
        this.rabbitMQFacadeForTest = new RabbitMQFacadeForTest();
        this.rabbitMQFacadeForTest.startRabbitMQSystem();
        this.config = new ClaimQueryUpdaterServiceConfiguration();
        this.config.amqpHost = "localhost";
        this.config.amqpUsername = "admin";
        this.config.amqpPassword = "admin";

        this.claimQueryService = new InMemoryClaimQueryService();
        this.service = new ClaimQueryUpdaterHttpService(this.config, claimQueryService);
        this.service.start();
        Thread.sleep(500);
    }

    @After
    public void after() throws IOException, TimeoutException, JSONException, UnirestException {
        this.rabbitMQFacadeForTest.stopRabbitMQSystem();
        this.service.stop();
    }

    @Test
    public void testUpdatesStatusToRegistered() throws IOException, TimeoutException, InterruptedException {
        ClaimDto claim = new SystemUnderTest().getSampleClaim();
        claim.id = UUID.randomUUID().toString();

        ClaimRegisteredEvent claimRegisteredEvent = new ClaimRegisteredEvent();
        claimRegisteredEvent.claim =claim;
        claimRegisteredEvent.id = claim.id;
        this.rabbitMQFacadeForTest.publishAsJson(this.config.claimRegistrationServiceExchangeName, ClaimRegisteredEvent.NAME, claimRegisteredEvent);

        Thread.sleep(500);

        ClaimDto claimFromDb = this.claimQueryService.findClaimById(claim.id);

        Assert.assertEquals(claimFromDb.status, "registered");
    }

    @Test
    public void testUpdatesStatusToVerified() throws IOException, TimeoutException, InterruptedException {
        ClaimDto claim = new SystemUnderTest().getSampleClaim();
        claim.id = UUID.randomUUID().toString();
        claim.status = "testing";
        this.claimQueryService.save(claim);

        ClaimVerifiedEvent claimVerifiedEvent = new ClaimVerifiedEvent();
        claimVerifiedEvent.claim =claim;
        claimVerifiedEvent.id = claim.id;
        this.rabbitMQFacadeForTest.publishAsJson(this.config.claimFraudServiceExchangeName, ClaimVerifiedEvent.NAME, claimVerifiedEvent);

        Thread.sleep(500);

        ClaimDto claimFromDb = this.claimQueryService.findClaimById(claim.id);

        Assert.assertEquals(claimFromDb.status, "verified");
    }

    @Test
    public void testUpdatesStatusToAwarded() throws IOException, TimeoutException, InterruptedException {
        ClaimDto claim = new SystemUnderTest().getSampleClaim();
        claim.id = UUID.randomUUID().toString();
        claim.status = "testing";
        this.claimQueryService.save(claim);

        ClaimAwardedEvent claimAwardedEvent = new ClaimAwardedEvent();
        claimAwardedEvent.claim =claim;
        claimAwardedEvent.id = claim.id;
        this.rabbitMQFacadeForTest.publishAsJson(this.config.claimAwardServiceExchangeName, ClaimAwardedEvent.NAME, claimAwardedEvent);

        Thread.sleep(500);

        ClaimDto claimFromDb = this.claimQueryService.findClaimById(claim.id);

        Assert.assertEquals(claimFromDb.status, "awarded");
    }

    @Test
    public void testUpdatesStatusToPaid() throws IOException, TimeoutException, InterruptedException {
        ClaimDto claim = new SystemUnderTest().getSampleClaim();
        claim.id = UUID.randomUUID().toString();
        claim.status = "testing";
        this.claimQueryService.save(claim);

        ClaimAwardPaidEvent claimAwardPaidEvent = new ClaimAwardPaidEvent();
        claimAwardPaidEvent.claim =claim;
        claimAwardPaidEvent.id = claim.id;
        this.rabbitMQFacadeForTest.publishAsJson(this.config.claimPaymentServiceExchangeName, ClaimAwardPaidEvent.NAME, claimAwardPaidEvent);

        Thread.sleep(500);

        ClaimDto claimFromDb = this.claimQueryService.findClaimById(claim.id);

        Assert.assertEquals(claimFromDb.status, "paid");
    }
}
