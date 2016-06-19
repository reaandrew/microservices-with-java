package uk.co.andrewrea.claim.portal.config;

/**
 * Created by vagrant on 6/15/16.
 */
public class ClaimPortalServiceConfiguration {
    public int servicePort = 8080;
    public String serviceIp = "127.0.0.1";
    public String claimRegistrationServiceUrl = "http://localhost:8083";
    public String claimQueryServiceUrl = "http://localhost:8081";
}
