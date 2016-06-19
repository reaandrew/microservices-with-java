package uk.co.andrewrea.claim.registration.services;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.gson.Gson;
import com.rabbitmq.client.*;
import spark.Service;
import uk.co.andrewrea.claim.registration.config.ClaimRegistrationConfiguration;
import uk.co.andrewrea.claim.registration.domain.core.ClaimService;
import uk.co.andrewrea.claim.registration.domain.events.publish.ClaimRegisteredEvent;
import uk.co.andrewrea.claim.registration.domain.dtos.ClaimDto;
import uk.co.andrewrea.claim.registration.domain.models.*;
import uk.co.andrewrea.infrastructure.inproc.Retry;
import uk.co.andrewrea.infrastructure.spark.JsonTransformer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Created by vagrant on 5/11/16.
 */
public class ClaimRegistrationHttpService {
    private Service service;
    private final HealthCheckRegistry healthChecks = new HealthCheckRegistry();
    private ClaimRegistrationConfiguration config;
    private ClaimService claimService;

    public ClaimRegistrationHttpService(ClaimRegistrationConfiguration configuration, ClaimService claimService) {
        this.config = configuration;
        this.claimService = claimService;
        this.healthChecks.register("application", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });
    }

    public void start() throws IOException, TimeoutException {
        //TODO: Health check to ensure the Exchange is present in RabbitMQ

        this.service = Service.ignite().port(config.servicePort).ipAddress(config.serviceIp);

        Connection conn = Retry.io(() -> {
            //Create a connection
            ConnectionFactory factory = new ConnectionFactory();
            factory.setVirtualHost("/");
            factory.setHost(this.config.amqpHost);
            factory.setPort(this.config.amqpPort);
            factory.setUsername(this.config.amqpUsername);
            factory.setPassword(this.config.amqpPassword);
            factory.setAutomaticRecoveryEnabled(true);

            return factory.newConnection();
        });

        Channel channel = conn.createChannel();
        //Create the host exchange
        channel.exchangeDeclare(this.config.claimRegistrationServiceExchangeName, "topic", false);

        //TODO: Need to change this so that we have the claim submitted workflow which we are missing.
        this.service.post("claims", (req, res) -> {
            ClaimDto claimDto = new Gson().fromJson(req.body(), ClaimDto.class);

            ClaimInfo info =  new ClaimInfo(claimDto.income,claimDto.passportNumber);
            Name name = new Name(claimDto.firstname, claimDto.middlenames, claimDto.surname);
            uk.co.andrewrea.claim.registration.domain.models.Address address = new uk.co.andrewrea.claim.registration.domain.models.Address(claimDto.address.line1,
                    claimDto.address.line2,
                    claimDto.address.town,
                    claimDto.address.city,
                    claimDto.address.postCode);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date dob = sdf.parse(claimDto.dob);
            ContactDetails contactDetails = new ContactDetails(claimDto.email, claimDto.receiveEmail);
            PersonalDetails personalDetails = new PersonalDetails(name,address,dob,contactDetails);
            BankAccount bankAccount = new BankAccount(claimDto.bankAccount.name, claimDto.bankAccount.sortCode, claimDto.bankAccount.number);
            Claim claim = new Claim(info,personalDetails, bankAccount);

            this.claimService.submitClaim(claim);

            ClaimRegisteredEvent claimRegisteredEvent = new ClaimRegisteredEvent();
            claimRegisteredEvent.id = claim.getId();
            claimRegisteredEvent.claim = claimDto;

            byte[] messageBodyBytes = new Gson().toJson(claimRegisteredEvent).getBytes();

            AMQP.BasicProperties messageProperties = new AMQP.BasicProperties.Builder()
                    .contentType("application/json")
                    .build();

            channel.basicPublish(config.claimRegistrationServiceExchangeName, ClaimRegisteredEvent.NAME, messageProperties, messageBodyBytes);


            res.status(202);
            HashMap returnValue =  new HashMap();
            returnValue.put("id",claimRegisteredEvent.id);

            return returnValue;
        }, new JsonTransformer());

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
