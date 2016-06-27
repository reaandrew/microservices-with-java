package uk.co.andrewrea.claim.award.domain.events.publish;

import uk.co.andrewrea.claim.award.domain.dtos.ClaimDto;

import java.time.Instant;
import java.util.Date;

/**
 * Created by vagrant on 5/13/16.
 */
public class ClaimAwardedEvent {
    public static final String NAME = "claim-awarded-event";

    public ClaimAwardedEvent(){
        this.timstamp = Instant.now().getEpochSecond();
    }

    public long timstamp;
    public String id;
    public ClaimDto claim;
}
