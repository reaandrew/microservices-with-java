package uk.co.andrewrea.claim.query.updater.eventHandlers;

import com.google.gson.Gson;
import uk.co.andrewrea.claim.query.core.ClaimQueryService;
import uk.co.andrewrea.claim.query.domain.dtos.ClaimDto;
import uk.co.andrewrea.claim.query.domain.events.subscribe.ClaimRegisteredEvent;
import uk.co.andrewrea.claim.query.domain.events.subscribe.ClaimVerifiedEvent;
import uk.co.andrewrea.claim.query.updater.core.EventHandler;

/**
 * Created by vagrant on 6/19/16.
 */
public class ClaimVerifiedEventHandler implements EventHandler {

    private ClaimQueryService claimQueryService;

    public ClaimVerifiedEventHandler(ClaimQueryService claimQueryService){

        this.claimQueryService = claimQueryService;
    }

    @Override
    public void handle(byte[] data) {
        ClaimVerifiedEvent claimVerifiedEvent = new Gson().fromJson(new String(data), ClaimVerifiedEvent.class);
        System.out.println(String.format("Received Claim Verified Event : %s", claimVerifiedEvent.id));

        ClaimDto claim = this.claimQueryService.findClaimById(claimVerifiedEvent.id);

        claim.status = "verified";
        claimQueryService.save(claim);
    }
}
