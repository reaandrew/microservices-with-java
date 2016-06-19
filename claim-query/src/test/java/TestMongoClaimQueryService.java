import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;
import uk.co.andrewrea.claim.query.config.ClaimQueryServiceConfiguration;
import uk.co.andrewrea.claim.query.domain.dtos.ClaimDto;
import uk.co.andrewrea.claim.query.infrastructure.mongo.MongoClaimQueryService;

/**
 * Created by vagrant on 6/19/16.
 */
public class TestMongoClaimQueryService {

    @Test
    public void testFindsAClaim(){
        MongoClient mongoClient = new MongoClient("0.0.0.0");

        MongoDatabase db = mongoClient.getDatabase("test");

        ClaimQueryServiceConfiguration config = new ClaimQueryServiceConfiguration();

        MongoClaimQueryService service = new MongoClaimQueryService(db, config.mongoClaimQueryCollectionName);

        ClaimDto claim = new SystemUnderTest().getSampleClaim();

        Gson gson = new Gson();

        db.getCollection(config.mongoClaimQueryCollectionName).insertOne(Document.parse(gson.toJson(claim)));

        ClaimDto claimFromDb = service.FindClaimById(claim.id);

        Assert.assertEquals(claim.id, claimFromDb.id);
    }
}
