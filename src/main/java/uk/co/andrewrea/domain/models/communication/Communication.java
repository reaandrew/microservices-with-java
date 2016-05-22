package uk.co.andrewrea.domain.models.communication;

/**
 * Created by vagrant on 5/22/16.
 */
public class Communication {
    private final String claimId;
    private final String email;

    public Communication(String claimId, String email){

        this.claimId = claimId;
        this.email = email;
    }

    public String getClaimId() {
        return claimId;
    }

    public String getEmail() {
        return email;
    }
}
