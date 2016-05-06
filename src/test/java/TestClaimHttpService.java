import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;
import uk.co.andrewrea.ClaimHttpService;

import java.util.HashMap;

/**
 * Created by vagrant on 5/6/16.
 */
public class TestClaimHttpService {

    private static ClaimHttpService service;

    @BeforeClass
    public static void before(){
        service = new ClaimHttpService(8080);
        service.start();
    }

    @AfterClass
    public static void after(){
        service.stop();
    }

    @Test
    public void claimReturnsReceived() throws UnirestException, JSONException {

        HashMap body = new HashMap();
        body.put("firstname","John");
        body.put("middlenames","Joseph");
        body.put("surname", "Doe");
        body.put("dob","1983/04/21");
        body.put("nino","AB000000A");
        body.put("income",21000);

        HttpResponse<JsonNode> response = Unirest.post("http://localhost:8080/claims")
                .body(new JSONObject(body).toString())
                .asJson();

        Assert.assertEquals(response.getBody().getObject().getString("status"), "received");

    }

    @Test
    public void serviceReturnsHealthyStatus() throws UnirestException, JSONException {
        HttpResponse<JsonNode> response = Unirest.get("http://localhost:8080/health")
                .asJson();

        JSONArray healthChecks = response.getBody().getArray();
        Assert.assertEquals(healthChecks.getJSONObject(0).getBoolean("healthy"), true);
    }
}
