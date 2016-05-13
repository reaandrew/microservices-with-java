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
import uk.co.andrewrea.domain.dtos.ClaimDto;
import uk.co.andrewrea.domain.events.ClaimRegisteredEvent;
import uk.co.andrewrea.events.Publisher;
import uk.co.andrewrea.infrastructure.rabbitmq.RabbitMQPublisher;
import uk.co.andrewrea.services.ClaimRegistrationHttpService;
import utils.RabbitMQExpections;
import utils.TestIdGenerator;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/11/16.
 */
public class TestClaimRegistrationHttpService {
    private TestIdGenerator idGenerator = new TestIdGenerator();
    private SystemUnderTest sut = new SystemUnderTest();
    private ClaimConfiguration claimServiceConfiguration;
    private ClaimRegistrationHttpService service;

    @Test
    public void publishesClaimRegisteredEvent() throws IOException, UnirestException, TimeoutException, InterruptedException {

        this.sut.startRabbitMQSystem();

        this.sut.setupExchangeFor(ClaimRegistrationHttpService.NAME);

        this.claimServiceConfiguration = new ClaimConfiguration();

        Channel channel = this.sut.createLocalRabbitMQChannel();
        Publisher publisher = new RabbitMQPublisher(channel, ClaimRegistrationHttpService.NAME);

        Service http = Service.ignite().port(this.claimServiceConfiguration.port);
        this.service = new ClaimRegistrationHttpService(this.idGenerator, http, publisher);
        this.service.start();

        RabbitMQExpections rabbitMQExpectations = new RabbitMQExpections(sut.createLocalRabbitMQChannel());
        rabbitMQExpectations.ExpectForExchange(ClaimRegistrationHttpService.NAME, messages -> {
            return messages.size() == 1 && messages.get(0).envelope.getRoutingKey().equals(ClaimRegisteredEvent.NAME);
        });

        ClaimDto claim = sut.getSampleClaim();

        HttpResponse<String> response = Unirest.post(String.format("http://localhost:%d/claims", this.claimServiceConfiguration.port))
                .body(new JSONObject(claim).toString())
                .asString();

        Assert.assertEquals(202, response.getCode());


        try {
            rabbitMQExpectations.VerifyAllExpectations();
        }
        finally{
            this.sut.stopRabbitMQSystem();
            this.service.stop();
        }

    }



    public void publishesClaimAwardedEvent(){

    }

    public void publishesClaimCompletedEvent() {

    }

}
