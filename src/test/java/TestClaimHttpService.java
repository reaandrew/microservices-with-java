import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.rabbitmq.client.Channel;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import spark.Service;
import uk.co.andrewrea.config.ClaimConfiguration;
import uk.co.andrewrea.domain.events.ClaimRegisteredEvent;
import uk.co.andrewrea.events.Publisher;
import uk.co.andrewrea.infrastructure.rabbitmq.RabbitMQPublisher;
import uk.co.andrewrea.services.ClaimHttpService;
import utils.RabbitMQExpections;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/11/16.
 */
public class TestClaimHttpService {
    private SystemUnderTest sut;

    @Before
    public void Before() throws IOException, TimeoutException {
        this.sut = new SystemUnderTest();
        this.sut.startRabbitMQSystem();
    }

    @After
    public void After() throws IOException, TimeoutException {
        this.sut.stopRabbitMQSystem();
    }

    @Test
    public void publishesClaimRegisteredEvent() throws IOException, UnirestException, TimeoutException {

        this.sut.setupExchangeFor(ClaimHttpService.NAME);

        ClaimConfiguration claimConfiguration = new ClaimConfiguration();

        Channel channel = this.sut.createLocalRabbitMQChannel();
        Publisher publisher = new RabbitMQPublisher(channel, ClaimHttpService.NAME);

        Service http = Service.ignite().port(claimConfiguration.port);
        ClaimHttpService service = new ClaimHttpService(http, publisher);
        service.start();

        RabbitMQExpections rabbitMQExpectations = new RabbitMQExpections(sut.createLocalRabbitMQChannel());
        rabbitMQExpectations.ExpectForExchange(ClaimHttpService.NAME, messages -> {
            return messages.size() == 1 && messages.get(0).envelope.getRoutingKey().equals(ClaimRegisteredEvent.NAME);
        });

        HashMap claim = sut.getSampleClaim();

        HttpResponse<String> response = Unirest.post(String.format("http://localhost:%d/claims", claimConfiguration.port))
                .body(new JSONObject(claim).toString())
                .asString();

        Assert.assertEquals(202, response.getCode());

        rabbitMQExpectations.VerifyAllExpectations();

        service.stop();

    }

    public void publishesClaimVerifiedEvent(){

    }

    public void publishesClaimAwardedEvent(){

    }

    public void publishesClaimCompletedEvent() {

    }

}
