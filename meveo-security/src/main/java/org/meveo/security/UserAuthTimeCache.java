package org.meveo.security;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Edward P. Legaspi
 * 
 **/
public class UserAuthTimeCache {

    private static final ConcurrentHashMap<String, Instant> LAST_AUTH_INSTANT_BY_USERNAME = new ConcurrentHashMap<>();

    public Instant getLoggingDate(String username) {
        return LAST_AUTH_INSTANT_BY_USERNAME.get(username);
    }

    public boolean updateLoggingDateIfNeeded(String username, Instant instant) {
        LAST_AUTH_INSTANT_BY_USERNAME.putIfAbsent(username, Instant.MIN);
        Instant oldValue = LAST_AUTH_INSTANT_BY_USERNAME.get(username);
        if (oldValue.plus(Duration.ofMinutes(1L)).isAfter(instant)) {
            return false;
        }

        return LAST_AUTH_INSTANT_BY_USERNAME.replace(username, oldValue, instant);
    }

}
