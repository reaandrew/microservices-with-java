package uk.co.andrewrea.claim.communication.domain.events.subscribe;

import uk.co.andrewrea.claim.communication.domain.dtos.ClaimDto;

import java.time.Instant;

/**
 * Created by vagrant on 5/13/16.
 */
public class ClaimAwardPaidEvent {
    public static final String NAME = "claim-award-paid-event";

    public ClaimAwardPaidEvent(){
        this.timstamp = Instant.now().getEpochSecond();
    }

    public long timstamp;
    public String id;
    public ClaimDto claim;
}
