package uk.co.andrewrea.claim.query.services;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.gson.Gson;
import org.slf4j.LoggerFactory;
import spark.Service;
import uk.co.andrewrea.claim.query.config.ClaimQueryServiceConfiguration;
import uk.co.andrewrea.claim.query.core.ClaimQueryService;
import uk.co.andrewrea.claim.query.domain.dtos.ClaimDto;
import uk.co.andrewrea.infrastructure.spark.JsonTransformer;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 6/19/16.
 */
public class ClaimQueryHttpService {

    private Service service;
    private final HealthCheckRegistry healthChecks = new HealthCheckRegistry();
    private ClaimQueryServiceConfiguration config;
    private ClaimQueryService claimService;

    public ClaimQueryHttpService(ClaimQueryServiceConfiguration config, ClaimQueryService claimService){
        this.config = config;
        this.claimService = claimService;
        this.healthChecks.register("application", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });
    }

    public void start() throws IOException, TimeoutException {
        this.service = Service.ignite().port(config.servicePort).ipAddress(config.serviceIp);

        this.service.get("/claims/:id", (req,res) -> {
            System.out.println(String.format("Received a request for claim %s", req.params("id")));
            ClaimDto claim = this.claimService.findClaimById(req.params("id"));

            System.out.println(String.format("Finding claim for id %s = %s", req.params("id"), claim.id));

            if(claim == null){
                res.status(404);
                HashMap map = new HashMap<String, String>();
                map.put("status", "unknown");
                return map;
            }
            return claim;
        }, new JsonTransformer());


        this.service.exception(Exception.class, (exception, request, response) -> {
            // Handle the exception here
            LoggerFactory.getLogger(this.getClass()).error("An exception occurred", exception);
            System.out.println(exception);
        });

        this.service.get("/info", (req, res) -> {
            res.status(200);
            return "";
        });

        this.service.get("/health", (req, res) -> {
            res.status(200);

            return new Gson().toJson(healthChecks.runHealthChecks().values());
        });

        this.service.get("/metrics", (req, res) -> {
            res.status(200);
            return "";
        });

    }


    public void stop() {
        this.service.stop();
    }

}
