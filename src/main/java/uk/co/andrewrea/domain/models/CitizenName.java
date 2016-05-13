package uk.co.andrewrea.domain.models;

/**
 * Created by vagrant on 5/11/16.
 */
public class CitizenName {
    private String firstname;
    private String surname;
    private String middlenames;

    public CitizenName(String firstname, String middlenames, String surname){

        this.firstname = firstname;
        this.middlenames = middlenames;
        this.surname = surname;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getSurname() {
        return surname;
    }

    public String getMiddlenames() {
        return middlenames;
    }
}
