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
import uk.co.andrewrea.ClaimSubmissionHttpService;
import uk.co.andrewrea.Subscription;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static spark.Service.ignite;

/**
 * Created by vagrant on 5/6/16.
 */
public class TestClaimSubmissionHttpServiceWithCustom {

    private static ClaimSubmissionHttpService service;

    @BeforeClass
    public static void before(){
        service = new ClaimSubmissionHttpService(8080);
        service.start();
    }

    @AfterClass
    public static void after(){
        service.stop();
    }

    @Test
    public void claimReturnsReceived() throws UnirestException, JSONException {

        HashMap body = getClaimObject();

        HttpResponse<JsonNode> response = Unirest.post("http://localhost:8080/claims")
                .body(new JSONObject(body).toString())
                .asJson();

        Assert.assertEquals(response.getCode(),202);
        Assert.assertEquals(response.getBody().getObject().getString("status"), "received");
    }

    private HashMap getClaimObject() {
        HashMap body = new HashMap();
        body.put("firstname","John");
        body.put("middlenames","Joseph");
        body.put("surname", "Doe");
        body.put("dob","1983/04/21");
        body.put("nino","AB000000A");
        body.put("income",21000);
        return body;
    }

    @Test
    public void serviceReturnsHealthyStatus() throws UnirestException, JSONException {
        HttpResponse<JsonNode> response = Unirest.get("http://localhost:8080/health")
                .asJson();

        JSONArray healthChecks = response.getBody().getArray();
        Assert.assertEquals(healthChecks.getJSONObject(0).getBoolean("healthy"), true);
    }

    @Test
    public void serviceAcceptsSubscription() throws UnirestException {

        final CountDownLatch signal = new CountDownLatch(1);
        int port = 40001;
        Service http = ignite().port(port);

        http.post("/pub/claimReceived", (req,res)->{
            signal.countDown();
            res.status(200);
            return "";
        });

        String subscriptionUrl = String.format("http://localhost:%d/pub/claimReceived",port);

        HttpResponse<String> subResponse = Unirest.post("http://localhost:8080/sub")
                .body(new Gson().toJson(new Subscription(subscriptionUrl,"claimReceived")))
                .asString();

        Assert.assertEquals(subResponse.getCode(), 201);

        HashMap body = getClaimObject();

        HttpResponse<String> claimResponse = Unirest.post("http://localhost:8080/claims")
                .body(new JSONObject(body).toString())
                .asString();

        System.out.print(claimResponse.getBody());

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
                .body(new Gson().toJson(new Subscription(subscriptionUrl,"claimReceived")))
                .asString();
        
        http.stop();
    }

    @Test
    public void serviceRemovesSubscription() throws UnirestException, JSONException {

        int port = 40001;

        String subscriptionUrl = String.format("http://localhost:%d/pub/claimReceived",port);

        HttpResponse<String> subResponse = Unirest.post("http://localhost:8080/sub")
                .body(new Gson().toJson(new Subscription(subscriptionUrl,"claimReceived")))
                .asString();

        HttpResponse<String> subDeleteResponse = Unirest.delete("http://localhost:8080/sub")
                .body(new Gson().toJson(new Subscription(subscriptionUrl,"claimReceived")))
                .asString();

        Assert.assertEquals(subDeleteResponse.getCode(), 200);

        HttpResponse<JsonNode> subGetResponse = Unirest.get("http://localhost:8080/sub")
                .asJson();

        JSONArray subs = subGetResponse.getBody().getArray();
        JSONObject claimReceivedSub = subs.getJSONObject(0);

        Assert.assertEquals(0,claimReceivedSub.getJSONArray("claimReceived").length());

    }
}
