package uk.co.andrewrea.infrastructure.rabbitmq.test;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

/**
 * Created by vagrant on 5/11/16.
 */
public class RabbitMQMessage {
    public String consumerTag;
    public Envelope envelope;
    public AMQP.BasicProperties properties;
    public byte[] body;

    public RabbitMQMessage(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
        this.consumerTag = consumerTag;
        this.envelope = envelope;
        this.properties = properties;
        this.body = body;
    }
}
