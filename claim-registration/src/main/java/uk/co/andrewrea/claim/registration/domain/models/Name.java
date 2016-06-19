package uk.co.andrewrea.claim.registration.domain.models;

/**
 * Created by vagrant on 6/19/16.
 */
public class Name {
    private final String firstname;
    private final String middlenames;
    private final String surname;

    public Name(String firstname, String middlenames, String surname){

        this.firstname = firstname;
        this.middlenames = middlenames;
        this.surname = surname;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getMiddlenames() {
        return middlenames;
    }


    public String getSurname() {
        return surname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Name name = (Name) o;

        if (getFirstname() != null ? !getFirstname().equals(name.getFirstname()) : name.getFirstname() != null)
            return false;
        if (getMiddlenames() != null ? !getMiddlenames().equals(name.getMiddlenames()) : name.getMiddlenames() != null)
            return false;
        return getSurname() != null ? getSurname().equals(name.getSurname()) : name.getSurname() == null;

    }

    @Override
    public int hashCode() {
        int result = getFirstname() != null ? getFirstname().hashCode() : 0;
        result = 31 * result + (getMiddlenames() != null ? getMiddlenames().hashCode() : 0);
        result = 31 * result + (getSurname() != null ? getSurname().hashCode() : 0);
        return result;
    }
}
