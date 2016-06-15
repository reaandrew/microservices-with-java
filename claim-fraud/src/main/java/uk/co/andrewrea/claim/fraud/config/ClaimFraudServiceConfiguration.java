package uk.co.andrewrea.claim.fraud.config;

/**
 * Created by vagrant on 5/13/16.
 */
public class ClaimFraudServiceConfiguration {
    public int servicePort = 8084;
    public String serviceIp = "127.0.0.1";
    public int amqpPort = 5672;
    public String amqpHost = "127.0.0.1";
    public String amqpUsername;
    public String amqpPassword;
    public String claimRegistrationServiceExchangeName = "claim-registration-service";
    public String claimFraudServiceExchangeName = "claim-fraud-service";
}
