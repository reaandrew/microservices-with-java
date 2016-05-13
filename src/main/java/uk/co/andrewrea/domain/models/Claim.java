package uk.co.andrewrea.domain.models;

/**
 * Created by vagrant on 5/11/16.
 */
public class Claim {

    private PersonalDetails personalDetails;

    private PassportDetails passportDetails;
    private BankAccount bankAccount;

    public Claim(PersonalDetails personalDetails, PassportDetails passportNumber, BankAccount account){
        this.personalDetails = personalDetails;
        this.passportDetails = passportNumber;
        bankAccount = account;
    }

    public PersonalDetails getPersonalDetails() {
        return personalDetails;
    }

    public PassportDetails getPassportDetails() {
        return passportDetails;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }
}
