import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;
import spark.Service;
import uk.co.andrewrea.config.ClaimSubmissionConfiguration;
import uk.co.andrewrea.domain.events.ClaimSubmittedEvent;
import uk.co.andrewrea.services.ClaimSubmissionHttpService;
import uk.co.andrewrea.events.Subscription;
import uk.co.andrewrea.infrastructure.http.HttpPubSub;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static spark.Service.ignite;

/**
 * Created by vagrant on 5/6/16.
 */
public class TestClaimSubmissionHttpServiceWithHttpPubSub {

    private static ClaimSubmissionHttpService service;
    private static HttpPubSub publisher = new HttpPubSub();
    private static ClaimSubmissionConfiguration claimSubmissionConfiguration = new ClaimSubmissionConfiguration();

    @BeforeClass
    public static void before(){
        Service http = Service.ignite().port(claimSubmissionConfiguration.port);

        http.post("/sub", (req,res) -> {
            Subscription sub = new Gson().fromJson(req.body(), Subscription.class);

            publisher.addSubscription(sub);

            res.status(201);
            return "";
        });
        http.delete("/sub", (req,res) -> {
            Subscription sub = new Gson().fromJson(req.body(), Subscription.class);
            Boolean result = publisher.removeSubscription(sub);
            if(result){
                res.status(200);
            }else{
                res.status(404);
            }
            return "";
        });
        http.get("/sub", (req,res) -> new Gson().toJson(publisher.listSubscriptions()));
        service = new ClaimSubmissionHttpService(http, publisher);
        service.start();
    }

    @AfterClass
    public static void after(){
        service.stop();
    }

    @Test
    public void claimReturnsReceived() throws UnirestException, JSONException, InterruptedException {
        Thread.sleep(100);
        HashMap body = new SystemUnderTest().getSampleClaim();

        HttpResponse<JsonNode> response = Unirest.post("http://localhost:8080/claims")
                .body(new JSONObject(body).toString())
                .asJson();

        Assert.assertEquals(response.getCode(),202);
        Assert.assertEquals(response.getBody().getObject().getString("status"), "received");
    }


    @Test
    public void serviceReturnsHealthyStatus() throws UnirestException, JSONException {
        HttpResponse<JsonNode> response = Unirest.get("http://localhost:8080/health")
                .asJson();

        JSONArray healthChecks = response.getBody().getArray();
        Assert.assertEquals(healthChecks.getJSONObject(0).getBoolean("healthy"), true);
    }

    @Test
    public void serviceAcceptsSubscription() throws UnirestException, InterruptedException {

        final CountDownLatch signal = new CountDownLatch(1);
        int port = 40001;
        Service http = ignite().port(port);
        //Let the service start listening
        Thread.sleep(500);

        http.post(String.format("/pub/%s", ClaimSubmittedEvent.NAME), (req, res)->{
            signal.countDown();
            res.status(200);
            return "";
        });

        String subscriptionUrl = String.format("http://localhost:%d/pub/%s",port, ClaimSubmittedEvent.NAME);

        HttpResponse<String> subResponse = Unirest.post("http://localhost:8080/sub")
                .body(new Gson().toJson(new Subscription(subscriptionUrl,ClaimSubmittedEvent.NAME)))
                .asString();

        Assert.assertEquals(subResponse.getCode(), 201);

        HashMap body = new SystemUnderTest().getSampleClaim();

        HttpResponse<String> claimResponse = Unirest.post("http://localhost:8080/claims")
                .body(new JSONObject(body).toString())
                .asString();

        //This is how I have to synchronise the test for the time being.
        try {
            boolean triggered = signal.await(5, TimeUnit.SECONDS);
            if (!triggered){
                Assert.fail("Signal was not triggered");
            }
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }

        Unirest.delete("http://localhost:8080/sub")
                .body(new Gson().toJson(new Subscription(subscriptionUrl,ClaimSubmittedEvent.NAME)))
                .asString();

        http.stop();
    }

    @Test
    public void serviceRemovesSubscription() throws UnirestException, JSONException {

        int port = 40001;

        String subscriptionUrl = String.format("http://localhost:%d/pub/%s",port, ClaimSubmittedEvent.NAME);

        HttpResponse<String> subResponse = Unirest.post("http://localhost:8080/sub")
                .body(new Gson().toJson(new Subscription(subscriptionUrl, ClaimSubmittedEvent.NAME)))
                .asString();

        HttpResponse<String> subDeleteResponse = Unirest.delete("http://localhost:8080/sub")
                .body(new Gson().toJson(new Subscription(subscriptionUrl,ClaimSubmittedEvent.NAME)))
                .asString();

        Assert.assertEquals(subDeleteResponse.getCode(), 200);

        HttpResponse<JsonNode> subGetResponse = Unirest.get("http://localhost:8080/sub")
                .asJson();

        JSONArray subs = subGetResponse.getBody().getArray();

        Assert.assertEquals(0,subs.length());

    }
}
