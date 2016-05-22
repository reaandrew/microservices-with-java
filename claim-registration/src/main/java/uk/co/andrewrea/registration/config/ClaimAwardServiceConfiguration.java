package uk.co.andrewrea.registration.config;

/**
 * Created by vagrant on 5/13/16.
 */
public class ClaimAwardServiceConfiguration {
    public int port;
    public String claimAwardServiceExchangeName;
    public String claimFraudServiceExchangeName;

    public ClaimAwardServiceConfiguration(){
        this.port = 8082;
        this.claimAwardServiceExchangeName = "claim-award-service";
        this.claimFraudServiceExchangeName = "claim-fraud-service";
    }
}
