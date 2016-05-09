package uk.co.andrewrea;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.gson.Gson;
import com.mashape.unirest.http.Unirest;
import spark.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by vagrant on 5/6/16.
 */
public class ClaimSubmissionHttpService {

    private int port;
    private Service service;
    private final HealthCheckRegistry healthChecks = new HealthCheckRegistry();
    private final ConcurrentHashMap<String, Collection<Subscription>> subscriptions = new ConcurrentHashMap<>();

    public ClaimSubmissionHttpService(int port){
        this.port = port;
        healthChecks.register("application", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });

    }

    public void start(){
        Service http = Service.ignite().port(this.port);
        http.post("/claims", (req, res) -> {
            Claim claim = new Gson().fromJson(req.body(), Claim.class);
            res.status(202);

            if(this.subscriptions.containsKey("claimReceived")) {
                Collection<Subscription> subs = this.subscriptions.get("claimReceived");

                for (Subscription sub : subs) {
                    Unirest.post(sub.url)
                            .body(new Gson().toJson(claim))
                            .asString();
                }
            }

            return "{\"status\":\"received\"}";
        });
        http.exception(Exception.class, (e, request, response) -> {
            System.out.print(e.getMessage());
            response.status(404);
            response.body("Resource not found");
        });
        http.get("/info",(req,res) -> {
            res.status(200);
            return "";
        } );
        http.get("/health",(req,res) -> {
            res.status(200);

            return new Gson().toJson(healthChecks.runHealthChecks().values());
        } );
        http.get("/metrics",(req,res) -> {
            res.status(200);
            return "";
        } );
        http.post("/sub", (req,res) -> {
            Subscription sub = new Gson().fromJson(req.body(), Subscription.class);
            this.subscriptions.computeIfPresent(sub.event,(s, subs) -> {
                subs.add(sub);
                return subs;
            });
            this.subscriptions.computeIfAbsent(sub.event, s -> {
                ArrayList<Subscription> list = new ArrayList<Subscription>();
                list.add(sub);
                return list;
            });

            res.status(201);
            return "";
        });
        http.delete("/sub", (req,res) -> {
            Subscription sub = new Gson().fromJson(req.body(), Subscription.class);
            if(this.subscriptions.containsKey(sub.event)){
                this.subscriptions.compute(sub.event,(evt, subs) -> {
                    subs.removeIf(subscription -> subscription.event.equals(evt) && subscription.url.equals(sub.url));
                    return subs;
                });
                res.status(200);
                return "";
            }
            res.status(404);
            return "";
        });
        http.get("/sub", (req,res) -> new Gson().toJson(this.subscriptions));
        this.service = http;
    }

    public void stop(){
        this.service.stop();
    }
}
