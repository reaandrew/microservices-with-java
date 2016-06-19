package uk.co.andrewrea.claim.query.infrastructure.inproc;

import uk.co.andrewrea.claim.query.core.ClaimQueryService;
import uk.co.andrewrea.claim.query.domain.dtos.ClaimDto;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by vagrant on 6/19/16.
 */
public class InMemoryClaimQueryService implements ClaimQueryService {
    private ArrayList<ClaimDto> claims;

    public InMemoryClaimQueryService(){
        this.claims = new ArrayList<>();
    }

    @Override
    public ClaimDto findClaimById(String id) {
        Optional<ClaimDto> value = this.claims.stream().filter(claimDto -> claimDto.id.equals(id)).findFirst();
        if(value.isPresent()) {
            return value.get();
        }else{
            return null;
        }
    }

    @Override
    public void save(ClaimDto claim) {
        this.claims.add(claim);
    }
}
