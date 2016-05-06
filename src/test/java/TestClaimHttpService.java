import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import uk.co.andrewrea.ClaimHttpService;

import java.util.HashMap;

/**
 * Created by vagrant on 5/6/16.
 */
public class TestClaimHttpService {

    @Test
    public void doesSomething() throws UnirestException, JSONException {
        ClaimHttpService service = new ClaimHttpService(8080);
        service.start();

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

        service.stop();
    }
}
