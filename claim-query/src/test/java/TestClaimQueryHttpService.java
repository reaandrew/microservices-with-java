import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.Assert;
import org.junit.Test;
import uk.co.andrewrea.claim.query.config.ClaimQueryServiceConfiguration;
import uk.co.andrewrea.claim.query.domain.dtos.ClaimDto;
import uk.co.andrewrea.claim.query.infrastructure.inproc.InMemoryClaimQueryService;
import uk.co.andrewrea.claim.query.services.ClaimQueryHttpService;
import uk.co.andrewrea.infrastructure.spark.Settings;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 6/19/16.
 */
public class TestClaimQueryHttpService {

    @Test
    public void testReturnsQueryFromTheQueryStore() throws IOException, TimeoutException, InterruptedException, UnirestException {
        ClaimQueryServiceConfiguration config = new ClaimQueryServiceConfiguration();
        InMemoryClaimQueryService claimQueryService = new InMemoryClaimQueryService();

        ClaimDto claim =  new SystemUnderTest().getSampleClaim();
        claimQueryService.save(claim);

        ClaimQueryHttpService service = new ClaimQueryHttpService(config, claimQueryService);

        service.start();

        Thread.sleep(Settings.SERVER_INIT_WAIT);

        HttpResponse<String> response = Unirest.get(String.format("http://localhost:%d/claims/%s", config.servicePort, claim.id))
                .asString();

        ClaimDto claimFromService = new Gson().fromJson(response.getBody(), ClaimDto.class);

        Assert.assertEquals(claim.id, claimFromService.id);
    }
}
