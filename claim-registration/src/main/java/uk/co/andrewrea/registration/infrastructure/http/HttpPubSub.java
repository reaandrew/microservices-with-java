package uk.co.andrewrea.registration.infrastructure.http;

import com.google.gson.Gson;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import uk.co.andrewrea.infrastructure.core.Publisher;
import uk.co.andrewrea.registration.events.Subscription;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by vagrant on 5/9/16.
 */
public class HttpPubSub implements Publisher {

    private final ConcurrentHashMap<String, Collection<Subscription>> subscriptions = new ConcurrentHashMap<>();

    public void addSubscription(Subscription sub){
        this.subscriptions.computeIfPresent(sub.event,(s, subs) -> {
            subs.add(sub);
            return subs;
        });
        this.subscriptions.computeIfAbsent(sub.event, s -> {
            ArrayList<Subscription> list = new ArrayList<Subscription>();
            list.add(sub);
            return list;
        });
    }

    public Boolean removeSubscription(Subscription sub){
        if(this.subscriptions.containsKey(sub.event)){
            this.subscriptions.compute(sub.event,(evt, subs) -> {
                subs.removeIf(subscription -> subscription.event.equals(evt) && subscription.url.equals(sub.url));
                return subs;
            });
            return true;
        }
        return false;
    }

    public ArrayList<Subscription> listSubscriptions(){
        return this.subscriptions.values().stream().flatMap(Collection::stream).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public <T> void publish(T message, String eventType) throws IOException {
        if(this.subscriptions.containsKey(eventType)) {
            Collection<Subscription> subs = this.subscriptions.get(eventType);

            for (Subscription sub : subs) {
                try {
                    Unirest.post(sub.url)
                            .body(new Gson().toJson(message))
                            .asString();
                } catch (UnirestException e) {
                    throw new IOException(e.getMessage());
                }
            }
        }
    }
}
