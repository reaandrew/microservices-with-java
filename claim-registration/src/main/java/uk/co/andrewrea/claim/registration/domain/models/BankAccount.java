package uk.co.andrewrea.claim.registration.domain.models;

/**
 * Created by vagrant on 6/19/16.
 */
public class BankAccount {
    private final String name;
    private final String sortCode;
    private final String accountNumber;

    public BankAccount(String name, String sortCode, String accountNumber){

        this.name = name;
        this.sortCode = sortCode;
        this.accountNumber = accountNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BankAccount that = (BankAccount) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (sortCode != null ? !sortCode.equals(that.sortCode) : that.sortCode != null) return false;
        return accountNumber != null ? accountNumber.equals(that.accountNumber) : that.accountNumber == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (sortCode != null ? sortCode.hashCode() : 0);
        result = 31 * result + (accountNumber != null ? accountNumber.hashCode() : 0);
        return result;
    }
}
