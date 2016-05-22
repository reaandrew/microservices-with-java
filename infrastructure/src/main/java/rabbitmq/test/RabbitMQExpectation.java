package rabbitmq.test;

import java.util.ArrayList;

/**
 * Created by vagrant on 5/11/16.
 */
public interface RabbitMQExpectation {
    Boolean match(ArrayList<RabbitMQMessage> messages);
}
