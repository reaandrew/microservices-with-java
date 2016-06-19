package uk.co.andrewrea.claim.query.config;

/**
 * Created by vagrant on 6/19/16.
 */
public class ClaimQueryServiceConfiguration {

    public int servicePort = 8081;
    public String serviceIp = "127.0.0.1";
    public int mongoDbPort = 27017;
    public String mongoDbHost = "127.0.0.1";
    public String mongoDatabaseName = "claimQuery";
    public String mongoClaimQueryCollectionName = "claims";

}

