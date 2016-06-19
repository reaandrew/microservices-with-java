package uk.co.andrewrea.claim.registration.domain.models;

/**
 * Created by vagrant on 6/19/16.
 */
public class Address {

    private final String line1;
    private final String line2;
    private final String town;
    private final String city;
    private final String postcode;

    public Address(String line1, String line2, String town, String city, String postcode){

        this.line1 = line1;
        this.line2 = line2;
        this.town = town;
        this.city = city;
        this.postcode = postcode;
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

    public String getPostcode() {
        return postcode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        if (getLine1() != null ? !getLine1().equals(address.getLine1()) : address.getLine1() != null) return false;
        if (getLine2() != null ? !getLine2().equals(address.getLine2()) : address.getLine2() != null) return false;
        if (getTown() != null ? !getTown().equals(address.getTown()) : address.getTown() != null) return false;
        if (getCity() != null ? !getCity().equals(address.getCity()) : address.getCity() != null) return false;
        return getPostcode() != null ? getPostcode().equals(address.getPostcode()) : address.getPostcode() == null;

    }

    @Override
    public int hashCode() {
        int result = getLine1() != null ? getLine1().hashCode() : 0;
        result = 31 * result + (getLine2() != null ? getLine2().hashCode() : 0);
        result = 31 * result + (getTown() != null ? getTown().hashCode() : 0);
        result = 31 * result + (getCity() != null ? getCity().hashCode() : 0);
        result = 31 * result + (getPostcode() != null ? getPostcode().hashCode() : 0);
        return result;
    }
}
