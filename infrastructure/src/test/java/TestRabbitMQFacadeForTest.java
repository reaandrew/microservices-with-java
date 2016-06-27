import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.co.andrewrea.infrastructure.rabbitmq.test.RabbitMQFacadeForTest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 6/25/16.
 */
public class TestRabbitMQFacadeForTest {

    private RabbitMQFacadeForTest rabbitMQFacadeForTest;

    @Before
    public void before() throws TimeoutException, UnirestException, JSONException, IOException {
        this.rabbitMQFacadeForTest = new RabbitMQFacadeForTest();
        this.rabbitMQFacadeForTest.startRabbitMQSystem();
    }

    @After
    public void after() throws TimeoutException, UnirestException, JSONException, IOException {
        this.rabbitMQFacadeForTest.stopRabbitMQSystem();
    }

    @Test
    public void testSettingUpAnExchange() throws TimeoutException, UnirestException, JSONException, IOException {
        this.rabbitMQFacadeForTest.setupTopicExchangeFor("something");
    }
}
