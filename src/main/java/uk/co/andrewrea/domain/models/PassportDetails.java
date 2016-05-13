package uk.co.andrewrea.domain.models;

/**
 * Created by vagrant on 5/11/16.
 */
public class PassportDetails {
    private String number;
    private Boolean verified = false;

    public PassportDetails(String number){
        this.number = number;
    }


    public String getNumber() {
        return number;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void verify(){
        this.verified = true;
    }
}
