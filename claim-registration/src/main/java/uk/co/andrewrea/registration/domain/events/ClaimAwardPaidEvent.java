package uk.co.andrewrea.registration.domain.events;

import uk.co.andrewrea.registration.domain.dtos.ClaimDto;

/**
 * Created by vagrant on 5/13/16.
 */
public class ClaimAwardPaidEvent {
    public static final String NAME = "claim-award-paid-event";

    public String id;
    public ClaimDto claim;
}
