package uk.co.andrewrea.domain.events;

import uk.co.andrewrea.domain.dtos.AddressDto;
import uk.co.andrewrea.domain.dtos.BankAccountDto;
import uk.co.andrewrea.domain.dtos.ClaimDto;

/**
 * Created by vagrant on 5/10/16.
 */
public class ClaimRegisteredEvent {
    public static final String NAME = "claim-registered-event";

    public String id;
    public ClaimDto claim;
}
