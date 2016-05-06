package uk.co.andrewrea;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.gson.Gson;
import spark.Service;

/**
 * Created by vagrant on 5/6/16.
 */
public class ClaimHttpService {

    private int port;
    private Service service;
    private final HealthCheckRegistry healthChecks = new HealthCheckRegistry();

    public ClaimHttpService(int port){
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
        http.post("/claims", (q, a) -> "{\"status\":\"received\"}");
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
        this.service = http;
    }

    public void stop(){
        this.service.stop();
    }
}
