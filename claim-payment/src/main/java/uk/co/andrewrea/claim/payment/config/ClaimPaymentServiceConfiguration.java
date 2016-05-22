package uk.co.andrewrea.claim.payment.config;

/**
 * Created by vagrant on 5/13/16.
 */
public class ClaimPaymentServiceConfiguration {

    public int port;

    public String claimAwardedServiceExchangeName;
    public String claimPaymentServiceExchangeName;

    public ClaimPaymentServiceConfiguration(){
        this.port = 8083;
        this.claimAwardedServiceExchangeName = "claim-awarded-service";
        this.claimPaymentServiceExchangeName = "claim-payment-service";
    }
}
