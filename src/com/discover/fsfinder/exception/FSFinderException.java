package com.discover.fsfinder.exception;

public class FSFinderException extends RuntimeException {

    public FSFinderException(String message) {
        super(message);
    }
    
    public FSFinderException(Throwable t) {
    	super(t);
    }
    
    public FSFinderException(String message, Throwable t) {
    	super(message, t);
    }
}
