package uk.co.andrewrea.claim.query.updater.config;

/**
 * Created by vagrant on 6/19/16.
 */
public class ClaimQueryUpdaterServiceConfiguration {

    public int servicePort = 8082;
    public String serviceIp = "127.0.0.1";

    public int mongoDbPort = 27017;
    public String mongoDbHost = "127.0.0.1";
    public String mongoDatabaseName = "claimQuery";
    public String mongoClaimQueryCollectionName = "claims";

    public int amqpPort = 5672;
    public String amqpHost = "127.0.0.1";
    public String amqpUsername;
    public String amqpPassword;

    public String claimQueryUpdaterExchangeName = "claim-query-updater";
    public String claimAwardServiceExchangeName = "claim-award-service";
    public String claimFraudServiceExchangeName = "claim-fraud-service";
    public String claimPaymentServiceExchangeName = "claim-payment-service";
    public String claimCommunicationServiceExchangeName = "claim-communication-service";
    public String claimRegistrationServiceExchangeName = "claim-registration-service";
}
