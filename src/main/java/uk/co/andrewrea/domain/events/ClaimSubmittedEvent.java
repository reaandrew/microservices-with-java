package uk.co.andrewrea.domain.events;

import uk.co.andrewrea.domain.dtos.AddressDto;
import uk.co.andrewrea.domain.dtos.BankAccountDto;

/**
 * Created by vagrant on 5/10/16.
 */
public class ClaimSubmittedEvent {
    public static final String NAME = "claim-submitted-event";

    public String firstname;
    public String surname;
    public String middlenames;
    public String dob;
    public String nino;
    public Integer income;
    public AddressDto address;
    public BankAccountDto bankAccount;
    public String email;
    public Boolean receiveEmail;
}
