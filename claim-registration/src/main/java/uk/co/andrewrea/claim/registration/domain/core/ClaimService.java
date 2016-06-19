package uk.co.andrewrea.claim.registration.domain.core;

import uk.co.andrewrea.claim.registration.domain.models.Claim;

/**
 * Created by vagrant on 6/19/16.
 */
public interface ClaimService {
    public void submitClaim(Claim claim);
}
