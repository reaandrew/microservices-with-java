
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/9/16.
 */
public class SystemUnderTest {
    private Connection conn;
    private Collection<Channel> channels;
    private final int RABBITMQ_PORT = 5672;
    private final String RABBITMQ_IP = "localhost";

    public SystemUnderTest(){
        this.channels = new ArrayList<>();
    }

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

    public Channel createLocalRabbitMQChannel() throws IOException {
        if(this.conn == null){
            throw new RuntimeException("RabbitMQ connection has not yet been created. call :startRabbitMQSystem");
        }
        Channel channel = conn.createChannel();
        this.channels.add(channel);
        return channel;
    }

    public void startRabbitMQSystem() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setVirtualHost("/");
        factory.setHost(RABBITMQ_IP);
        factory.setPort(RABBITMQ_PORT);
        this.conn = factory.newConnection();
    }

    public void stopRabbitMQSystem() throws IOException, TimeoutException {

        for(Channel chan : this.channels) {
            chan.close();
        }
        this.conn.close();
    }

    public void setupExchangeFor(String name) throws IOException, TimeoutException {
        Channel channel = this.conn.createChannel();
        channel.exchangeDeclare(name,"topic",false);
        channel.close();
    }
}
