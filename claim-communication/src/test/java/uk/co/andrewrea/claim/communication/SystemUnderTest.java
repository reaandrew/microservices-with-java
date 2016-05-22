package uk.co.andrewrea.claim.communication;

import uk.co.andrewrea.communication.domain.dtos.AddressDto;
import uk.co.andrewrea.communication.domain.dtos.BankAccountDto;
import uk.co.andrewrea.communication.domain.dtos.ClaimDto;

/**
 * Created by vagrant on 5/9/16.
 */
public class SystemUnderTest {

    public ClaimDto getSampleClaim() {
        ClaimDto claim = new ClaimDto();
        claim.firstname = "John";
        claim.middlenames = "Jospeh";
        claim.surname = "Doe";
        claim.dob = "1983/04/21";
        claim.nino = "AB000000A";
        claim.income = 21000;
        claim.passportNumber = "123456789";

        BankAccountDto bankAccount = new BankAccountDto();
        bankAccount.name = "knowles and barclays";
        bankAccount.number = "87654321";
        bankAccount.sortCode = "00-00-00";
        claim.bankAccount = bankAccount;

        AddressDto address = new AddressDto();
        address.line1 = "10 Some Street";
        address.line2 = "Some Place";
        address.town = "Some Town";
        address.city = "Some City";
        address.postCode = "XX1 1XX";
        claim.address = address;

        claim.email = "john@not.exists";
        claim.receiveEmail = true;

        return claim;
    }

}
