package uk.co.andrewrea.claim.award.domain.events.subscribe;


import uk.co.andrewrea.claim.award.domain.dtos.ClaimDto;

/**
 * Created by vagrant on 5/11/16.
 */
public class ClaimVerifiedEvent {
    public static final String NAME = "claim-verified-event";

    public String id;
    public ClaimDto claim;
}
