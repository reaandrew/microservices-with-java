package uk.co.andrewrea.events;

/**
 * Created by vagrant on 5/8/16.
 */
public class Subscription {

    public Subscription(String url, String event){
        this.url = url;
        this.event = event;
    }

    public String url;
    public String event;
}
