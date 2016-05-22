package uk.co.andrewrea.infrastructure.rabbitmq.test;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/22/16.
 */
public class RabbitMQFacadeForTest {
    private Connection conn;


    private Collection<Channel> channels;
    private final int RABBITMQ_PORT = 5672;
    private final String RABBITMQ_IP = "localhost";

    public RabbitMQFacadeForTest(){
        this.channels = new ArrayList<>();
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

    public void setupTopicExchangeFor(String exchangeName) throws IOException, TimeoutException {
        Channel channel = this.conn.createChannel();
        channel.exchangeDeclare(exchangeName,"topic",false);
        channel.close();
    }

    public <T> void publishAsJson(String exchangeName, String routingKey, T message) throws IOException, TimeoutException {
        Channel channel = this.conn.createChannel();
        byte[] messageBodyBytes = new Gson().toJson(message).getBytes();

        AMQP.BasicProperties messageProperties = new AMQP.BasicProperties.Builder()
                .contentType("application/json")
                .build();

        channel.basicPublish(exchangeName, routingKey, messageProperties, messageBodyBytes);
        channel.close();
    }
}
