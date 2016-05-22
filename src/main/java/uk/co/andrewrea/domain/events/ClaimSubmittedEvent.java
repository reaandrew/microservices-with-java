package uk.co.andrewrea.domain.events;

import uk.co.andrewrea.domain.dtos.ClaimDto;

/**
 * Created by vagrant on 5/10/16.
 */
public class ClaimSubmittedEvent {
    public static final String NAME = "claim-submitted-event";

    public ClaimDto claim;
}
