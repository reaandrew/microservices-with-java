package uk.co.andrewrea.domain.events;

import uk.co.andrewrea.domain.dtos.AddressDto;
import uk.co.andrewrea.domain.dtos.BankAccountDto;

/**
 * Created by vagrant on 5/11/16.
 */
public class ClaimVerifiedEvent {
    public static final String NAME = "claim-verified-event";

    public String id;
    public String firstname;
    public String surname;
    public String middlenames;
    public String dob;
    public String nino;
    public Integer income;
    public String passportNumber;
    public AddressDto address;
    public BankAccountDto bankAccount;
}
