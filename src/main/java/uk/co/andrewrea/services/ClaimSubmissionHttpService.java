package uk.co.andrewrea.services;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.gson.Gson;
import spark.Service;
import uk.co.andrewrea.domain.events.ClaimSubmittedEvent;
import uk.co.andrewrea.events.Publisher;
import uk.co.andrewrea.domain.dtos.ClaimDto;

/**
 * Created by vagrant on 5/6/16.
 */
public class ClaimSubmissionHttpService {

    public static final String NAME = "ClaimSubmissionHttpService";

    private Service service;
    private Publisher publisher;
    private final HealthCheckRegistry healthChecks = new HealthCheckRegistry();

    public ClaimSubmissionHttpService(Service service, Publisher publisher){
        this.service = service;
        this.publisher = publisher;
        healthChecks.register("application", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });

    }

    public void start(){

        this.service.post("/claims", (req, res) -> {
            ClaimDto claim = new Gson().fromJson(req.body(), ClaimDto.class);
            res.status(202);

            ClaimSubmittedEvent evt = new ClaimSubmittedEvent();
            evt.claim = claim;

            this.publisher.publish(evt, ClaimSubmittedEvent.NAME);

            return "{\"status\":\"received\"}";
        });
        this.service.exception(Exception.class, (e, request, response) -> {
            System.out.print(e.getMessage());
            response.status(404);
            response.body("Resource not found");
        });
        this.service.get("/info",(req,res) -> {
            res.status(200);
            return "";
        } );
        this.service.get("/health",(req,res) -> {
            res.status(200);

            return new Gson().toJson(healthChecks.runHealthChecks().values());
        } );
        this.service.get("/metrics",(req,res) -> {
            res.status(200);
            return "";
        } );
    }

    public void stop(){
        this.service.stop();
    }
}
