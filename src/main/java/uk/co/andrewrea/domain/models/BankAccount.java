package uk.co.andrewrea.domain.models;

/**
 * Created by vagrant on 5/11/16.
 */
public class BankAccount {
    private String name;
    private String number;
    private String sortCode;
    private Boolean verified;

    public BankAccount(String  name, String number, String sortCode){

        this.name = name;
        this.number = number;
        this.sortCode = sortCode;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getSortCode() {
        return sortCode;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void verify(){
        this.verified = true;
    }
}
