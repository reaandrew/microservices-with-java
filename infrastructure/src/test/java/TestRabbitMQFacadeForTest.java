import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONException;
import org.junit.Test;
import uk.co.andrewrea.infrastructure.rabbitmq.test.RabbitMQFacadeForTest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 6/25/16.
 */
public class TestRabbitMQFacadeForTest {

    @Test
    public void testSomething() throws TimeoutException, UnirestException, JSONException, IOException {
        RabbitMQFacadeForTest rabbitMQFacadeForTest = new RabbitMQFacadeForTest();
        for(int i = 0; i < 10; i++) {
            rabbitMQFacadeForTest.startRabbitMQSystem();
            rabbitMQFacadeForTest.stopRabbitMQSystem();
        }
    }
}
