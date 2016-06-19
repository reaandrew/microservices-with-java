package uk.co.andrewrea.claim.query.updater.eventHandlers;

import com.google.gson.Gson;
import uk.co.andrewrea.claim.query.core.ClaimQueryService;
import uk.co.andrewrea.claim.query.domain.dtos.ClaimDto;
import uk.co.andrewrea.claim.query.domain.events.subscribe.ClaimAwardPaidEvent;
import uk.co.andrewrea.claim.query.domain.events.subscribe.ClaimAwardedEvent;
import uk.co.andrewrea.claim.query.updater.core.EventHandler;

/**
 * Created by vagrant on 6/19/16.
 */
public class ClaimAwardPaidEventHandler implements EventHandler {

    private ClaimQueryService claimQueryService;

    public ClaimAwardPaidEventHandler(ClaimQueryService claimQueryService){

        this.claimQueryService = claimQueryService;
    }

    @Override
    public void handle(byte[] data) {
        ClaimAwardPaidEvent claimAwardPaidEvent = new Gson().fromJson(new String(data), ClaimAwardPaidEvent.class);
        ClaimDto claim = this.claimQueryService.findClaimById(claimAwardPaidEvent.id);

        claim.status = "paid";
        claimQueryService.save(claim);
    }
}
