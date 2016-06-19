package uk.co.andrewrea.claim.query.updater.eventHandlers;

import com.google.gson.Gson;
import uk.co.andrewrea.claim.query.core.ClaimQueryService;
import uk.co.andrewrea.claim.query.domain.dtos.ClaimDto;
import uk.co.andrewrea.claim.query.domain.events.subscribe.ClaimRegisteredEvent;
import uk.co.andrewrea.claim.query.updater.core.EventHandler;

/**
 * Created by vagrant on 6/19/16.
 */
public class ClaimRegisteredEventHandler implements EventHandler {

    private ClaimQueryService claimQueryService;

    public ClaimRegisteredEventHandler(ClaimQueryService claimQueryService){

        this.claimQueryService = claimQueryService;
    }

    @Override
    public void handle(byte[] data) {
        ClaimRegisteredEvent claimRegisteredEvent = new Gson().fromJson(new String(data), ClaimRegisteredEvent.class);
        ClaimDto claim = this.claimQueryService.findClaimById(claimRegisteredEvent.id);

        if(claim == null){
            claim = claimRegisteredEvent.claim;
        }

        claim.status = "registered";
        claim.id = claimRegisteredEvent.id;
        claimQueryService.save(claim);
    }
}
