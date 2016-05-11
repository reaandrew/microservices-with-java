
import com.rabbitmq.client.Channel;

import java.util.HashMap;

/**
 * Created by vagrant on 5/9/16.
 */
public class SystemUnderTest {
    public HashMap getSampleClaim() {
        HashMap body = new HashMap();
        body.put("firstname","John");
        body.put("middlenames","Joseph");
        body.put("surname", "Doe");
        body.put("dob","1983/04/21");
        body.put("nino","AB000000A");
        body.put("income",21000);
        return body;
    }
}
