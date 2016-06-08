package org.rso.network.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NodeNotFoundException extends RuntimeException {
    public NodeNotFoundException() {
        super();
    }

    public NodeNotFoundException(String message) {
        super(message);
    }
}
