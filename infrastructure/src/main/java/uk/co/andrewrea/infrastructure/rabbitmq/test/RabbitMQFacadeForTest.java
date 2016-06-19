package uk.co.andrewrea.infrastructure.rabbitmq.test;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.json.JSONArray;
import org.json.JSONException;

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
    private static final int RABBITMQ_PORT = 5672;
    private static final int RABBITMQ_API_PORT = 15672;
    private static final String RABBITMQ_IP = "localhost";
    private static final String RABBITMQ_UN = "admin";
    private static final String RABBITMQ_PW = "admin";

    public RabbitMQFacadeForTest() {
        this.channels = new ArrayList<>();
    }

    public Channel createLocalRabbitMQChannel() throws IOException {
        if (this.conn == null) {
            throw new RuntimeException("RabbitMQ connection has not yet been created. call :startRabbitMQSystem");
        }
        Channel channel = conn.createChannel();
        this.channels.add(channel);
        return channel;
    }

    public void startRabbitMQSystem() throws IOException, TimeoutException, JSONException, UnirestException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setVirtualHost("/");
        factory.setHost(RABBITMQ_IP);
        factory.setPort(RABBITMQ_PORT);
        this.conn = factory.newConnection();
        this.deleteAllQueues();
    }

    public void stopRabbitMQSystem() throws IOException, TimeoutException, JSONException, UnirestException {
        this.deleteAllQueues();
        for (Channel chan : this.channels) {
            chan.close();
        }
        this.conn.close();
    }

    public void deleteAllQueues() throws UnirestException, JSONException, IOException, TimeoutException {
        String url = String.format("http://%s:%s@localhost:%d/api/queues",RABBITMQ_UN,RABBITMQ_PW, RABBITMQ_API_PORT);
        HttpResponse<JsonNode> response = Unirest.get(url).asJson();

        Channel channel = conn.createChannel();
        try {
            JSONArray queues = response.getBody().getArray();
            for (int i = 0; i < queues.length(); i++) {
                String queueName = queues.getJSONObject(i).getString("name");
                channel.queueDelete(queueName);
            }
        }finally {
            channel.close();
        }
    }

    public void setupTopicExchangeFor(String exchangeName) throws IOException, TimeoutException {
        Channel channel = this.conn.createChannel();
        channel.exchangeDeclare(exchangeName, "topic", false);
        channel.close();
    }

    public <T> void publishAsJson(String exchangeName, String routingKey, T message) throws IOException, TimeoutException {
        Channel channel = null;

        channel = this.conn.createChannel();
        byte[] messageBodyBytes = new Gson().toJson(message).getBytes();

        AMQP.BasicProperties messageProperties = new AMQP.BasicProperties.Builder()
                .contentType("application/json")
                .build();

        channel.basicPublish(exchangeName, routingKey, messageProperties, messageBodyBytes);
        channel.close();


    }
}
