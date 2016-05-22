package uk.co.andrewrea.claim.communication.domain.events.subscribe;

import uk.co.andrewrea.claim.communication.domain.dtos.ClaimDto;

/**
 * Created by vagrant on 5/10/16.
 */
public class ClaimRegisteredEvent {
    public static final String NAME = "claim-registered-event";

    public String id;
    public ClaimDto claim;
}
