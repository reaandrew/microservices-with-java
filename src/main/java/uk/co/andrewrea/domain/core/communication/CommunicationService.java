package uk.co.andrewrea.domain.core.communication;

import uk.co.andrewrea.domain.models.communication.Communication;

import java.util.Optional;

/**
 * Created by vagrant on 5/22/16.
 */
public interface CommunicationService {
    void save(Communication communication);
    void send(Communication communication);
    Optional<Communication> getByClaimId(String claimId);
}
