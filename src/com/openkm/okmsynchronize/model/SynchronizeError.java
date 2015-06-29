/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.model;

/**
 *
 * @author abujosa
 */
public class SynchronizeError extends Exception {
    
    private String message;
    private String description;
    private Throwable error;

    public SynchronizeError(String message, String description, Throwable error) {
        this.message = message;
        this.description = description;
        this.error = error;
    }

    public SynchronizeError(String message, String description) {
        this.message = message;
        this.description = description;
    }
    
    public String getMessage() { return message; }

    public String getDescription() { return description; }

    public Throwable getError() { return error; }    
    
}
