package utils;

import uk.co.andrewrea.core.IdGenerator;

import java.util.UUID;

/**
 * Created by vagrant on 5/11/16.
 */
public class TestIdGenerator implements IdGenerator {

    private String id;

    @Override
    public String generateID() {
        if(this.id == null){
            return UUID.randomUUID().toString();
        }else{
            String idToReturn = id;
            this.id = null;
            return idToReturn;
        }
    }

    public void next(String id){
        this.id = id;
    }
}
