package org.rso.storage.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by Radosław on 28.05.2016.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class LocationDoesNotExistException extends RuntimeException {
    public LocationDoesNotExistException() {
    }

    public LocationDoesNotExistException(String message) {
        super(message);
    }

    public LocationDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public LocationDoesNotExistException(Throwable cause) {
        super(cause);
    }

    public LocationDoesNotExistException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
