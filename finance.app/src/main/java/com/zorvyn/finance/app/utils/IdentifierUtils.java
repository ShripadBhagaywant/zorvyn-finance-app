package com.zorvyn.finance.app.utils;

import com.zorvyn.finance.app.exception.InvalidUuidException;

import java.util.UUID;

public class IdentifierUtils {

    public static UUID parseUuid(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new InvalidUuidException("The provided ID format is invalid: " + id);
        }
    }

}
