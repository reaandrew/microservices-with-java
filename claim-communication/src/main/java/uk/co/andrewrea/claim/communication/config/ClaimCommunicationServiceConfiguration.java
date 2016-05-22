package uk.co.andrewrea.claim.communication.config;

/**
 * Created by vagrant on 5/22/16.
 */
public class ClaimCommunicationServiceConfiguration {
    public int servicePort = 8087;
    public String serviceIp = "127.0.0.1";
    public int amqpPort = 5672;
    public String amqpHost = "127.0.0.1";

    public String claimPaymentServiceExchangeName= "claim-payment-service";
    public String claimCommunicationServiceExchangeName= "claim-communication-service";
    public String claimRegistrationServiceExchangeName= "claim-registration-service";

    public ClaimCommunicationServiceConfiguration(){
        this.claimPaymentServiceExchangeName = "claim-payment-service";
        this.claimCommunicationServiceExchangeName = "claim-communication-service";
        this.claimRegistrationServiceExchangeName = "claim-registration-service";
    }
}
