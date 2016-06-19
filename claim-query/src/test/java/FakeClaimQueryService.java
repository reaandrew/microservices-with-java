import uk.co.andrewrea.claim.query.core.ClaimQueryService;
import uk.co.andrewrea.claim.query.domain.dtos.ClaimDto;

import java.util.ArrayList;

/**
 * Created by vagrant on 6/19/16.
 */
public class FakeClaimQueryService implements ClaimQueryService {
    private ArrayList<ClaimDto> claims;

    public FakeClaimQueryService(){
        this.claims = new ArrayList<>();
    }

    @Override
    public ClaimDto FindClaimById(String id) {
        return this.claims.stream().filter(claimDto -> claimDto.id.equals(id)).findFirst().get();
    }

    public void load(ClaimDto claim){
        this.claims.add(claim);
    }
}
