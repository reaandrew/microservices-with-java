package uk.co.andrewrea.claim.query.infrastructure.mongo;

import com.google.gson.Gson;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import uk.co.andrewrea.claim.query.core.ClaimQueryService;
import uk.co.andrewrea.claim.query.domain.dtos.ClaimDto;

/**
 * Created by vagrant on 6/19/16.
 */
public class MongoClaimQueryService implements ClaimQueryService {

    private MongoCollection<Document> claimCollection;

    public MongoClaimQueryService(MongoDatabase database, String mongoQueryCollectionName){
        this.claimCollection = database.getCollection(mongoQueryCollectionName);
    }

    @Override
    public ClaimDto FindClaimById(String id) {
        FindIterable<Document> iterable = this.claimCollection.find(Filters.eq("id", id));

        ClaimDto claimFromDb = new Gson().fromJson(iterable.first().toJson(), ClaimDto.class);

        return claimFromDb;
    }
}
