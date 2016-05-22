package uk.co.andrewrea.infrastructure.rabbitmq;

import com.rabbitmq.client.Address;

/**
 * Created by vagrant on 5/22/16.
 */
public class AmqpExchange {
    public String name;
    public int port;
    public String ip;

    public AmqpExchange(){
        this.port = 5672;
        this.ip = "127.0.0.1";
    }

    public AmqpExchange(String name){
        this();
        this.name = name;
    }

    public AmqpExchange(String name, String ip){
        this(name);
        this.ip = ip;
    }

    public AmqpExchange(String name, String ip, int port){
        this(name, ip);
        this.port = port;
    }
}
