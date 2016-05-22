package uk.co.andrewrea.communication.config;

/**
 * Created by vagrant on 5/22/16.
 */
public class ClaimCommunicationServiceConfiguration {
    public int port;

    public String claimPaymentServiceExchangeName;
    public String claimCommunicationServiceExchangeName;
    public String claimRegistrationServiceExchangeName;

    public ClaimCommunicationServiceConfiguration(){
        this.port = 8084;
        this.claimPaymentServiceExchangeName = "claim-payment-service";
        this.claimCommunicationServiceExchangeName = "claim-communication-service";
        this.claimRegistrationServiceExchangeName = "claim-registration-service";
    }
}
