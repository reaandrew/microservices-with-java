package uk.co.andrewrea.claim.registration.infrastructure.mongo;

import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import uk.co.andrewrea.claim.registration.domain.core.ClaimService;
import uk.co.andrewrea.claim.registration.domain.models.Claim;

/**
 * Created by vagrant on 6/19/16.
 */
public class MongoClaimService implements ClaimService {
    private MongoCollection<Document> claimCollection;

    public MongoClaimService(MongoDatabase mongoDatabase, String mongoClaimCollectionName) {

        this.claimCollection = mongoDatabase.getCollection(mongoClaimCollectionName);
    }

    @Override
    public void submitClaim(Claim claim) {
        Document document = Document.parse(new Gson().toJson(claim));
        document.put("_id", claim.getId());
        this.claimCollection.insertOne(document);
    }
}
