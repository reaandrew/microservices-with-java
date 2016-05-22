package uk.co.andrewrea.claim.award.domain.events.publish;

import uk.co.andrewrea.claim.award.domain.dtos.ClaimDto;

/**
 * Created by vagrant on 5/13/16.
 */
public class ClaimAwardedEvent {
    public static final String NAME = "claim-awarded-event";

    public String id;
    public ClaimDto claim;
}
