package uk.co.andrewrea.claim.payment.domain.events.publish;

import uk.co.andrewrea.claim.payment.domain.dtos.ClaimDto;

/**
 * Created by vagrant on 5/13/16.
 */
public class ClaimAwardPaidEvent {
    public static final String NAME = "claim-award-paid-event";

    public String id;
    public ClaimDto claim;
}
