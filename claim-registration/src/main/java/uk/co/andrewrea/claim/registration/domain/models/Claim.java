package uk.co.andrewrea.claim.registration.domain.models;

import java.util.UUID;

/**
 * Created by vagrant on 6/19/16.
 */
public class Claim {

    private String id;
    private ClaimInfo claimInfo;
    private final PersonalDetails personalDetails;
    private final BankAccount bankAccount;

    public Claim(ClaimInfo claimInfo, PersonalDetails personalDetails, BankAccount bankAccount){
        this.id = UUID.randomUUID().toString();

        this.claimInfo = claimInfo;

        this.personalDetails = personalDetails;
        this.bankAccount = bankAccount;
    }

    public String getId() {
        return id;
    }

    public ClaimInfo getClaimInfo() {
        return claimInfo;
    }

    public PersonalDetails getPersonalDetails() {
        return personalDetails;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Claim claim = (Claim) o;

        if (getId() != null ? !getId().equals(claim.getId()) : claim.getId() != null) return false;
        if (getClaimInfo() != null ? !getClaimInfo().equals(claim.getClaimInfo()) : claim.getClaimInfo() != null)
            return false;
        if (getPersonalDetails() != null ? !getPersonalDetails().equals(claim.getPersonalDetails()) : claim.getPersonalDetails() != null)
            return false;
        return getBankAccount() != null ? getBankAccount().equals(claim.getBankAccount()) : claim.getBankAccount() == null;

    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getClaimInfo() != null ? getClaimInfo().hashCode() : 0);
        result = 31 * result + (getPersonalDetails() != null ? getPersonalDetails().hashCode() : 0);
        result = 31 * result + (getBankAccount() != null ? getBankAccount().hashCode() : 0);
        return result;
    }
}
