package uk.co.andrewrea.communication.infrastructure.inproc;

import uk.co.andrewrea.communication.domain.core.CommunicationService;
import uk.co.andrewrea.communication.domain.models.Communication;
import uk.co.andrewrea.infrastructure.core.EmailService;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by vagrant on 5/22/16.
 */
public class InProcCommunicationService implements CommunicationService {

    public static final String EMAIL_FROM = "claims@does.not.exist";

    private EmailService emailService;
    private ArrayList<Communication> communications;

    public InProcCommunicationService(EmailService emailService){
        this.communications = new ArrayList<>();
        this.emailService = emailService;
    }

    @Override
    public void save(Communication communication) {
        Boolean exists = this.communications.stream().anyMatch(comm -> comm.getClaimId().equals(communication.getClaimId()));
        if(!exists) {
            this.communications.add(communication);
        }else {
        throw new RuntimeException("Claim communication already exists in the collection");
        }
    }

    @Override
    public void send(Communication communication) {
        this.emailService.sendEmail(communication.getEmail(), EMAIL_FROM, "Claim has been sent");
    }

    @Override
    public Optional<Communication> getByClaimId(String claimId) {
        for(int i = 0; i < this.communications.size(); i++){
            if(this.communications.get(i).getClaimId().equals(claimId)){
                return Optional.of(this.communications.get(i));
            }
        }
        return Optional.empty();
    }
}
