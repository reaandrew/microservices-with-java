package uk.co.andrewrea.claim.communication.domain.core;

import uk.co.andrewrea.claim.communication.domain.models.Communication;

import java.util.Optional;

/**
 * Created by vagrant on 5/22/16.
 */
public interface CommunicationService {
    void save(Communication communication);
    void send(Communication communication);
    Optional<Communication> getByClaimId(String claimId);
}
