package uk.co.andrewrea;

/**
 * Created by vagrant on 5/8/16.
 */
public class Subscription {

    public Subscription(){}

    public Subscription(String url, String event){
        this.url = url;
        this.event = event;
    }

    public String url;
    public String event;
}
