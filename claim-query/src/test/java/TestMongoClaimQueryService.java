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

        ClaimQueryServiceConfiguration config = new ClaimQueryServiceConfiguration();

        MongoClient mongoClient = new MongoClient(config.mongoDbHost, config.mongoDbPort);

        MongoDatabase db = mongoClient.getDatabase("test");

        MongoClaimQueryService service = new MongoClaimQueryService(db, config.mongoClaimQueryCollectionName);

        ClaimDto claim = new SystemUnderTest().getSampleClaim();

        Gson gson = new Gson();

        Document document = Document.parse(gson.toJson(claim));
        document.put("_id", claim.id);
        db.getCollection(config.mongoClaimQueryCollectionName).insertOne(document);

        ClaimDto claimFromDb = service.findClaimById(claim.id);

        Assert.assertEquals(claim.id, claimFromDb.id);
    }
}
