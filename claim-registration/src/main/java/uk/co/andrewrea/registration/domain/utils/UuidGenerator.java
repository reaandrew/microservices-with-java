package uk.co.andrewrea.registration.domain.utils;

import uk.co.andrewrea.registration.core.IdGenerator;

import java.util.UUID;

/**
 * Created by vagrant on 5/11/16.
 */
public class UuidGenerator implements IdGenerator {
    @Override
    public String generateID() {
        return UUID.randomUUID().toString();
    }
}
