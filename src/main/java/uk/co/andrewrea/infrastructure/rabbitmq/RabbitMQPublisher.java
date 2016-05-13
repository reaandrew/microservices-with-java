package uk.co.andrewrea.infrastructure.rabbitmq;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import uk.co.andrewrea.events.Publisher;

import java.io.IOException;

/**
 * Created by vagrant on 5/9/16.
 */
public class RabbitMQPublisher implements Publisher {

    private final Channel channel;
    private final String exchangeName;

    public RabbitMQPublisher(Channel channel, String exchangeName){

        this.channel = channel;
        this.exchangeName = exchangeName;
    }

    @Override
    public <T> void publish(T message, String eventType) throws IOException {
        byte[] messageBodyBytes = new Gson().toJson(message).getBytes();

        AMQP.BasicProperties messageProperties = new AMQP.BasicProperties.Builder()
                .contentType("application/json")
                .build();

        channel.basicPublish(exchangeName, eventType, messageProperties, messageBodyBytes);
    }

    public static RabbitMQPublisher create(Channel channel, String exchangeName) throws IOException {
        channel.exchangeDeclare(exchangeName,"topic",false);
        return new RabbitMQPublisher(channel, exchangeName);
    }
}
