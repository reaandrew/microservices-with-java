package uk.co.andrewrea.claim.query.updater.eventHandlers;

import com.google.gson.Gson;
import uk.co.andrewrea.claim.query.core.ClaimQueryService;
import uk.co.andrewrea.claim.query.domain.dtos.ClaimDto;
import uk.co.andrewrea.claim.query.domain.events.subscribe.ClaimAwardedEvent;
import uk.co.andrewrea.claim.query.domain.events.subscribe.ClaimVerifiedEvent;
import uk.co.andrewrea.claim.query.updater.core.EventHandler;

/**
 * Created by vagrant on 6/19/16.
 */
public class ClaimAwardedEventHandler implements EventHandler {

    private ClaimQueryService claimQueryService;

    public ClaimAwardedEventHandler(ClaimQueryService claimQueryService){

        this.claimQueryService = claimQueryService;
    }

    @Override
    public void handle(byte[] data) {
        ClaimAwardedEvent claimAwardedEvent = new Gson().fromJson(new String(data), ClaimAwardedEvent.class);
        ClaimDto claim = this.claimQueryService.findClaimById(claimAwardedEvent.id);

        claim.status = "awarded";
        claimQueryService.save(claim);
    }
}
