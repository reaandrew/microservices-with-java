
import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import uk.co.andrewrea.domain.dtos.AddressDto;
import uk.co.andrewrea.domain.dtos.BankAccountDto;
import uk.co.andrewrea.domain.dtos.ClaimDto;

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

    public ClaimDto getSampleClaim() {

        ClaimDto claim = new ClaimDto();
        claim.firstname = "John";
        claim.middlenames = "Jospeh";
        claim.surname = "Doe";
        claim.dob = "1983/04/21";
        claim.nino = "AB000000A";
        claim.income = 21000;
        claim.passportNumber = "123456789";

        BankAccountDto bankAccount = new BankAccountDto();
        bankAccount.name = "knowles and barclays";
        bankAccount.number = "87654321";
        bankAccount.sortCode = "00-00-00";
        claim.bankAccount = bankAccount;

        AddressDto address = new AddressDto();
        address.line1 = "10 Some Street";
        address.line2 = "Some Place";
        address.town = "Some Town";
        address.city = "Some City";
        address.postCode = "XX1 1XX";
        claim.address = address;

        return claim;
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

    public <T> void publishTo(String exchangeName, String eventType, T message) throws IOException, TimeoutException {
        Channel channel = this.conn.createChannel();
        byte[] messageBodyBytes = new Gson().toJson(message).getBytes();

        AMQP.BasicProperties messageProperties = new AMQP.BasicProperties.Builder()
                .contentType("application/json")
                .build();

        channel.basicPublish(exchangeName, eventType, messageProperties, messageBodyBytes);
        channel.close();
    }
}
