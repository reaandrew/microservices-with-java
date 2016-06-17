package uk.co.andrewrea.claim.portal.services;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.github.javafaker.Faker;
import com.google.gson.Gson;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.Service;
import spark.Spark;
import spark.template.handlebars.HandlebarsTemplateEngine;
import uk.co.andrewrea.claim.portal.config.ClaimPortalServiceConfiguration;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
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

            if(req.queryParams("test") != null){
                Faker faker = new Faker();
                String firstname = faker.name().firstName();
                String lastname = faker.name().lastName();

                map.put("firstName", firstname);
                map.put("middleNames", faker.name().firstName());
                map.put("lastName", lastname);

                Date from = new GregorianCalendar(1950, Calendar.JANUARY, 11).getTime();
                Date to = new GregorianCalendar(1998, Calendar.JANUARY, 11).getTime();
                Date date = faker.date().between(from, to);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                map.put("dob", sdf.format(date));

                map.put("income", faker.number().numberBetween(8000,23000));
                map.put("passportNo", Integer.toString((80000000 + (int)(Math.random()* 10000000))));

                map.put("line1", faker.address().streetAddress());
                map.put("line2", faker.address().secondaryAddress());
                map.put("town", faker.address().state());
                map.put("city", faker.address().cityName());

                Random r = new Random();
                String postcode = String.format("%s%s%s %s%s%s",
                        (char)(r.nextInt(26) + 'a'),
                        (char)(r.nextInt(26) + 'a'),
                        r.nextInt(99),
                        r.nextInt(9),
                        (char)(r.nextInt(26) + 'a'),
                        (char)(r.nextInt(26) + 'a')).toUpperCase();
                map.put("postcode", postcode);

                map.put("bankName", faker.app().name());
                map.put("sortCode", 100000 + (int)(Math.random()*600000));
                map.put("accountNumber", 10000000 + (int)(Math.random()*80000000));

                String email = String.format("%s.%s@%s.doesnot.exist", firstname, lastname, faker.app().name());
                map.put("email",email.replace(' ','-').toLowerCase() );
                map.put("receiveEmail", "checked");

            }
            return new ModelAndView(map,"claim-form.hbs");
        }, new HandlebarsTemplateEngine());

        /*
        this.service.post("/claims", (req,res) -> {

        });
        */

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
