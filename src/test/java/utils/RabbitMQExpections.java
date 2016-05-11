package utils;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.*;

/**
 * Created by vagrant on 5/11/16.
 */
public class RabbitMQExpections {
    private Channel channel;
    private static final Map<String,ArrayList<RabbitMQMessage>> messages = new HashMap<>();
    private static final Map<String, ArrayList<RabbitMQExpectation>> expectations = new HashMap<>();

    public RabbitMQExpections(Channel channel) {
        this.channel = channel;
    }

    public RabbitMQExpections ExpectForExchange(String name, RabbitMQExpectation expectation) throws IOException {
        if (!this.messages.containsKey(name)){
            this.messages.put(name, new ArrayList<>());
        }
        if(!this.expectations.containsKey(name)){
            this.expectations.put(name, new ArrayList<>());
        }
        this.expectations.get(name).add(expectation);

        String queueName = UUID.randomUUID().toString();
        //Create a queue and bind to the exchange
        channel.queueDeclare(queueName,false, true, true, null);
        channel.queueBind(queueName, name, "*");
        //Create a consumer of the queue
        Runnable consumer = () -> {
            boolean autoAck = false;
            try {
                channel.basicConsume(queueName, autoAck,
                        new DefaultConsumer(channel) {
                            @Override
                            public void handleDelivery(String consumerTag,
                                                       Envelope envelope,
                                                       AMQP.BasicProperties properties,
                                                       byte[] body)
                                    throws IOException
                            {
                                messages.get(name).add(new RabbitMQMessage(consumerTag,envelope,properties,body));
                                long deliveryTag = envelope.getDeliveryTag();
                                channel.basicAck(deliveryTag, false);
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        consumer.run();
        return this;
    }

    public void VerifyAllExpectations(){
        for(Map.Entry<String, ArrayList<RabbitMQExpectation>> expectationsForExchange : expectations.entrySet()){
            ArrayList<RabbitMQMessage> input = messages.get(expectationsForExchange.getKey());
            for(RabbitMQExpectation expectation : expectationsForExchange.getValue()){
                if(!expectation.match(input)){
                    throw new RuntimeException(String.format("Expectations failed for Exchange: %s [failed]", expectationsForExchange.getKey()));
                }
            }
        }
    }

}
