package uk.co.andrewrea.registration.config;

/**
 * Created by vagrant on 5/13/16.
 */
public class ClaimFraudServiceConfiguration {
    public int port;
    public String claimRegistrationServiceExchangeName;
    public String claimFraudServiceExchangeName;

    public ClaimFraudServiceConfiguration(){
        this.port = 8081;
        this.claimRegistrationServiceExchangeName = "claim-registration-service";
        this.claimFraudServiceExchangeName = "claim-fraud-service";
    }
}
