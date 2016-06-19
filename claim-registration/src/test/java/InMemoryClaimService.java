import uk.co.andrewrea.claim.registration.domain.core.ClaimService;
import uk.co.andrewrea.claim.registration.domain.models.Claim;

import java.util.ArrayList;

/**
 * Created by vagrant on 6/19/16.
 */
public class InMemoryClaimService implements ClaimService {

    private ArrayList<Claim> claims;

    public InMemoryClaimService(){
        this.claims = new ArrayList<>();
    }

    @Override
    public void submitClaim(Claim claim) {
        this.claims.add(claim);
    }

    public int getNumberOfClaimsSubmitted(){
        return this.claims.size();
    }
}
