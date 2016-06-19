package uk.co.andrewrea.claim.registration.domain.models;

/**
 * Created by vagrant on 6/19/16.
 */
public class ContactDetails {
    private final String email;
    private final Boolean receiveEmail;

    public ContactDetails(String email, Boolean receiveEmail){

        this.email = email;
        this.receiveEmail = receiveEmail;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getReceiveEmail() {
        return receiveEmail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContactDetails that = (ContactDetails) o;

        if (getEmail() != null ? !getEmail().equals(that.getEmail()) : that.getEmail() != null) return false;
        return getReceiveEmail() != null ? getReceiveEmail().equals(that.getReceiveEmail()) : that.getReceiveEmail() == null;

    }

    @Override
    public int hashCode() {
        int result = getEmail() != null ? getEmail().hashCode() : 0;
        result = 31 * result + (getReceiveEmail() != null ? getReceiveEmail().hashCode() : 0);
        return result;
    }
}
