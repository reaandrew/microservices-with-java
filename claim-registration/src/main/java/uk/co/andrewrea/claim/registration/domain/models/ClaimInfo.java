package uk.co.andrewrea.claim.registration.domain.models;

/**
 * Created by vagrant on 6/19/16.
 */
public class ClaimInfo {
    private final int income;
    private final String passportNumber;

    public ClaimInfo(int income, String passportNumber){

        this.income = income;
        this.passportNumber = passportNumber;
    }

    public int getIncome() {
        return income;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClaimInfo claimInfo = (ClaimInfo) o;

        if (getIncome() != claimInfo.getIncome()) return false;
        return getPassportNumber() != null ? getPassportNumber().equals(claimInfo.getPassportNumber()) : claimInfo.getPassportNumber() == null;

    }

    @Override
    public int hashCode() {
        int result = getIncome();
        result = 31 * result + (getPassportNumber() != null ? getPassportNumber().hashCode() : 0);
        return result;
    }
}
