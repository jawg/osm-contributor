package io.jawg.osmcontributor.rest;

import java.io.IOException;

public class NetworkException extends IOException {

    public NetworkException() {
        super("No network");
    }
}
