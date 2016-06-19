package uk.co.andrewrea.claim.registration.domain.models;

import java.util.Date;

/**
 * Created by vagrant on 6/19/16.
 */
public class PersonalDetails {
    private Name name;
    private Address address;
    private Date dob;
    private ContactDetails contactDetails;

    public PersonalDetails(Name name, Address address, Date dob, ContactDetails contactDetails){

        this.name = name;
        this.address = address;
        this.dob = dob;
        this.contactDetails = contactDetails;
    }

    public Name getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public Date getDob() {
        return dob;
    }

    public ContactDetails getContactDetails() {
        return contactDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PersonalDetails that = (PersonalDetails) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getAddress() != null ? !getAddress().equals(that.getAddress()) : that.getAddress() != null) return false;
        if (getDob() != null ? !getDob().equals(that.getDob()) : that.getDob() != null) return false;
        return getContactDetails() != null ? getContactDetails().equals(that.getContactDetails()) : that.getContactDetails() == null;

    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getAddress() != null ? getAddress().hashCode() : 0);
        result = 31 * result + (getDob() != null ? getDob().hashCode() : 0);
        result = 31 * result + (getContactDetails() != null ? getContactDetails().hashCode() : 0);
        return result;
    }
}
