package uk.co.andrewrea.domain.models;

/**
 * Created by vagrant on 5/11/16.
 */
public class Address {
    private String line1;
    private String line2;
    private String town;
    private String city;
    private String postCode;

    public Address(String line1, String line2, String town, String city, String postCode){

        this.line1 = line1;
        this.line2 = line2;
        this.town = town;
        this.city = city;
        this.postCode = postCode;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getTown() {
        return town;
    }

    public String getCity() {
        return city;
    }

    public String getPostCode() {
        return postCode;
    }
}
