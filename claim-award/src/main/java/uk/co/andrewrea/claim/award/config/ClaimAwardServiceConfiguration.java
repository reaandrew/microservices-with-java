package uk.co.andrewrea.claim.award.config;

public class ClaimAwardServiceConfiguration {
    public int servicePort = 8085;
    public String serviceIp = "127.0.0.1";
    public int amqpPort = 5672;
    public String amqpHost = "127.0.0.1";
    public String amqpUsername;
    public String amqpPassword;
    public String claimAwardServiceExchangeName= "claim-award-service";
    public String claimFraudServiceExchangeName = "claim-fraud-service";

}
