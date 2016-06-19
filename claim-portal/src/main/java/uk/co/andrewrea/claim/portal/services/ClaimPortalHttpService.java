package uk.co.andrewrea.claim.portal.services;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.github.javafaker.Faker;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.Service;
import spark.template.handlebars.HandlebarsTemplateEngine;
import uk.co.andrewrea.claim.portal.config.ClaimPortalServiceConfiguration;
import uk.co.andrewrea.claim.portal.domain.dtos.AddressDto;
import uk.co.andrewrea.claim.portal.domain.dtos.BankAccountDto;
import uk.co.andrewrea.claim.portal.domain.dtos.ClaimDto;
import uk.co.andrewrea.infrastructure.spark.JsonTransformer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeoutException;

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


        this.service.post("/claims", "application/json", (req,res) -> {

            MultiMap<String> params = new MultiMap<>();
            UrlEncoded.decodeTo(req.body(), params, "UTF-8");

            ClaimDto claim = new ClaimDto();
            claim.firstname = params.getString("firstname");
            claim.middlenames = params.getString("middlenames");
            claim.surname = params.getString("surname");
            claim.dob = params.getString("dob");
            try {
                claim.income = Integer.parseInt(params.getString("income"));
            }catch(NumberFormatException ex){
                claim.income = 0;
            }
            claim.passportNumber = params.getString("passportNo");

            claim.address = new AddressDto();
            claim.address.line1 = params.getString("line1");
            claim.address.line2 = params.getString("line2");
            claim.address.town = params.getString("town");
            claim.address.city = params.getString("city");
            claim.address.postCode = params.getString("postCode");

            claim.bankAccount = new BankAccountDto();
            claim.bankAccount.name = params.getString("bankName");
            claim.bankAccount.sortCode = params.getString("sortCode");
            claim.bankAccount.number = params.getString("accountNumber");

            claim.receiveEmail = params.getString("receiveEmail") != null;
            claim.email = params.getString("email");


            HttpResponse<JsonNode> response = Unirest.post(String.format("%s/claims", config.claimRegistrationServiceUrl))
                    .body(new Gson().toJson(claim))
                    .asJson();

            res.redirect("/claim/%s".format(response.getBody().getObject().getString("id")));

            res.status(200);

            return claim;
        }, new JsonTransformer());

        this.service.get("/claims/:id",(req,res) -> {

            HttpResponse<String> response = Unirest.post(String.format("%s/claims/%s", config.claimQueryServiceUrl, req.params("id")))
                    .asString();

            ClaimDto claim = new Gson().fromJson(response.getBody(), ClaimDto.class);

            return new ModelAndView(claim,"claim-details.hbs");
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
