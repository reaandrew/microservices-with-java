package uk.co.andrewrea.domain.events;

/**
 * Created by vagrant on 5/10/16.
 */
public class ClaimRegisteredEvent {
    public static final String NAME = "claim-registered-event";

    public String id;
    public String firstname;
    public String surname;
    public String middlenames;
    public String dob;
    public String nino;
    public Integer income;
}
