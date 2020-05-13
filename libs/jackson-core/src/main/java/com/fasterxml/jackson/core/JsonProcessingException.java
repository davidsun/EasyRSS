/* Jackson JSON-processor.
 *
 * Copyright (c) 2007- Tatu Saloranta, tatu.saloranta@iki.fi
 */

package com.fasterxml.jackson.core;

/**
 * Intermediate base class for all problems encountered when
 * processing (parsing, generating) JSON content
 * that are not pure I/O problems.
 * Regular {@link java.io.IOException}s will be passed through as is.
 * Sub-class of {@link java.io.IOException} for convenience.
 */
public class JsonProcessingException extends java.io.IOException
{
    final static long serialVersionUID = 123; // Stupid eclipse...
	
    protected JsonLocation _location;

    protected JsonProcessingException(String msg, JsonLocation loc, Throwable rootCause) {
        /* Argh. IOException(Throwable,String) is only available starting
         * with JDK 1.6...
         */
        super(msg);
        if (rootCause != null) {
            initCause(rootCause);
        }
        _location = loc;
    }

    protected JsonProcessingException(String msg) {
        super(msg);
    }

    protected JsonProcessingException(String msg, JsonLocation loc) {
        this(msg, loc, null);
    }

    protected JsonProcessingException(String msg, Throwable rootCause) {
        this(msg, null, rootCause);
    }

    protected JsonProcessingException(Throwable rootCause) {
        this(null, null, rootCause);
    }

    public JsonLocation getLocation() { return _location; }

    /*
    /**********************************************************
    /* Extended API
    /**********************************************************
     */

    /**
     * Method that allows accessing the original "message" argument,
     * without additional decorations (like location information)
     * that overridden {@link #getMessage} adds.
     * 
     * @since 2.1
     */
    public String getOriginalMessage() { return super.getMessage(); }

    /*
    /**********************************************************
    /* Methods for sub-classes to use, override
    /**********************************************************
     */
    
    /**
     * Accessor that sub-classes can override to append additional
     * information right after the main message, but before
     * source location information.
     */
    protected String getMessageSuffix() { return null; }

    /*
    /**********************************************************
    /* Overrides of standard methods
    /**********************************************************
     */
    
    /**
     * Default method overridden so that we can add location information
     */
    @Override public String getMessage() {
        String msg = super.getMessage();
        if (msg == null) {
            msg = "N/A";
        }
        JsonLocation loc = getLocation();
        String suffix = getMessageSuffix();
        // mild optimization, if nothing extra is needed:
        if (loc != null || suffix != null) {
            StringBuilder sb = new StringBuilder(100);
            sb.append(msg);
            if (suffix != null) {
                sb.append(suffix);
            }
            if (loc != null) {
                sb.append('\n');
                sb.append(" at ");
                sb.append(loc.toString());
            }
            msg = sb.toString();
        }
        return msg;
    }

    @Override public String toString() { return getClass().getName()+": "+getMessage(); }
}
