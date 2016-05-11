package uk.co.andrewrea.domain.events;

/**
 * Created by vagrant on 5/10/16.
 */
public class ClaimSubmittedEvent {
    public static final String NAME = "claim-submitted-event";

    public String firstname;
    public String surname;
    public String middlenames;
    public String dob;
    public String nino;
    public Integer income;
}
