package uk.co.andrewrea.claim.query.domain.dtos;

/**
 * Created by vagrant on 5/8/16.
 */
public class ClaimDto {
    public String id;
    public String firstname;
    public String surname;
    public String middlenames;
    public String dob;
    public Integer income;
    public String passportNumber;
    public AddressDto address;
    public BankAccountDto bankAccount;
    public String email;
    public Boolean receiveEmail;
    public String status;
}
