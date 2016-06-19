import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;
import uk.co.andrewrea.claim.registration.config.ClaimRegistrationConfiguration;
import uk.co.andrewrea.claim.registration.domain.dtos.ClaimDto;
import uk.co.andrewrea.claim.registration.domain.models.*;
import uk.co.andrewrea.claim.registration.infrastructure.mongo.MongoClaimService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by vagrant on 6/19/16.
 */
public class TestMongoClaimService {

    @Test
    public void testItSavesAClaim() throws ParseException {

        ClaimRegistrationConfiguration config = new ClaimRegistrationConfiguration();

        MongoClient mongoClient = new MongoClient(config.mongoDbHost, config.mongoDbPort);

        MongoDatabase db = mongoClient.getDatabase("test");
        
        MongoClaimService service = new MongoClaimService(db, config.mongoClaimCollectionName);

        ClaimDto claimDto = new SystemUnderTest().getSampleClaim();

        ClaimInfo info =  new ClaimInfo(claimDto.income,claimDto.passportNumber);
        Name name = new Name(claimDto.firstname, claimDto.middlenames, claimDto.surname);
        Address address = new Address(claimDto.address.line1,
                claimDto.address.line2,
                claimDto.address.town,
                claimDto.address.city,
                claimDto.address.postCode);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date dob = sdf.parse(claimDto.dob);
        ContactDetails contactDetails = new ContactDetails(claimDto.email, claimDto.receiveEmail);
        PersonalDetails personalDetails = new PersonalDetails(name,address,dob,contactDetails);
        BankAccount bankAccount = new BankAccount(claimDto.bankAccount.name, claimDto.bankAccount.sortCode, claimDto.bankAccount.number);
        Claim claim = new Claim(info,personalDetails, bankAccount);

        Gson gson = new Gson();

        service.submitClaim(claim);

        FindIterable<Document> iterable = db.getCollection("claims").find(Filters.eq("_id", claim.getId()));

        Claim claimFromDb = gson.fromJson(iterable.first().toJson(), Claim.class);
        System.out.println(gson.toJson(claimFromDb));
        Assert.assertEquals(claim, claimFromDb);
    }
}
