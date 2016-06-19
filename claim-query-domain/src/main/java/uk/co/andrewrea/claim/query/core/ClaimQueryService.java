package uk.co.andrewrea.claim.query.core;

import uk.co.andrewrea.claim.query.domain.dtos.ClaimDto;

import java.util.Collection;

/**
 * Created by vagrant on 6/19/16.
 */
public interface ClaimQueryService {

    public ClaimDto findClaimById(String id);

    void save(ClaimDto claim);
}
