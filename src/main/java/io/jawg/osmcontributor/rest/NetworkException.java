package io.jawg.osmcontributor.rest;

public class NetworkException extends RuntimeException {

    public NetworkException() {
        super("No network");
    }
}
