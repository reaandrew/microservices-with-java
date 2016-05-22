package uk.co.andrewrea.claim.payment.domain.events.subscribe;


import uk.co.andrewrea.claim.payment.domain.dtos.ClaimDto;

/**
 * Created by vagrant on 5/13/16.
 */
public class ClaimAwardedEvent {
    public static final String NAME = "claim-awarded-event";

    public String id;
    public ClaimDto claim;
}
