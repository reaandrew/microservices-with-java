package uk.co.andrewrea.registration.config;

/**
 * Created by vagrant on 5/10/16.
 */
public class ClaimRegistrationConfiguration {

    public int port;
    public String claimRegistrationServiceExchangeName;

    public ClaimRegistrationConfiguration(){
        this.port = 8081;
        this.claimRegistrationServiceExchangeName = "claim-registration-service";
    }
}
