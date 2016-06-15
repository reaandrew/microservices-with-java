package uk.co.andrewrea.claim.portal.services;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.gson.Gson;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.Service;
import spark.Spark;
import spark.template.handlebars.HandlebarsTemplateEngine;
import uk.co.andrewrea.claim.portal.config.ClaimPortalServiceConfiguration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static spark.Spark.staticFiles;

/**
 * Created by vagrant on 6/15/16.
 */
public class ClaimPortalHttpService {
    private Service service;
    private final HealthCheckRegistry healthChecks = new HealthCheckRegistry();
    private ClaimPortalServiceConfiguration config;

    public ClaimPortalHttpService(ClaimPortalServiceConfiguration config) {
        this.config = config;
        this.healthChecks.register("application", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });
    }

    public void start() throws IOException, TimeoutException {
        this.service = Service.ignite().port(config.servicePort).ipAddress(config.serviceIp);
        this.service.staticFileLocation("/public");

        this.service.get("/", (req,res) -> {
            Map map = new HashMap();
            map.put("name", "Sam");
            return new ModelAndView(map,"claim-form.hbs");
        }, new HandlebarsTemplateEngine());

        this.service.exception(Exception.class, (exception, request, response) -> {
            // Handle the exception here
             LoggerFactory.getLogger(this.getClass()).error("An exception occurred", exception);
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
