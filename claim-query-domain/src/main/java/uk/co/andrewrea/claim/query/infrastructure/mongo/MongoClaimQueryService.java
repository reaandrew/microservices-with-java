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
    public ClaimDto findClaimById(String id) {
        FindIterable<Document> iterable = this.claimCollection.find(Filters.eq("_id", id));

        if(iterable.iterator().hasNext()){
            ClaimDto claimFromDb = new Gson().fromJson(iterable.first().toJson(), ClaimDto.class);

            return claimFromDb;
        }else{
            return null;
        }


    }

    @Override
    public void save(ClaimDto claim) {
        String json = new Gson().toJson(claim);
        Document document = Document.parse(json);
        document.put("_id", claim.id);
        System.out.println(String.format("Mongo Document ID (_id) set to %s", claim.id));
        if(this.findClaimById(claim.id) == null){
            this.claimCollection.insertOne(document);
        }else{
            this.claimCollection.replaceOne(Filters.eq("_id", claim.id), document);
        }

    }
}
