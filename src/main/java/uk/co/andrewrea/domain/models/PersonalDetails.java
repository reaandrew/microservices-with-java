package uk.co.andrewrea.domain.models;

import java.util.Date;

/**
 * Created by vagrant on 5/11/16.
 */
public class PersonalDetails {
    private CitizenName name;
    private Date dob;
    private Nino nino;
    private Address address;
    private int income;

    public PersonalDetails(CitizenName name, Date dob, Nino nino, Address address, int income){

        this.name = name;
        this.dob = dob;
        this.nino = nino;
        this.address = address;
        this.income = income;
    }

    public CitizenName getName() {
        return name;
    }

    public Date getDob() {
        return dob;
    }

    public Nino getNino() {
        return nino;
    }

    public Address getAddress() {
        return address;
    }

    public int getIncome() {
        return income;
    }
}
