/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openkm.okmsynchronize.utils;

/**
 *
 * @author abujosa
 */
public class SynchronizeException extends Exception {

    public SynchronizeException() {
        super();
    }
    
    public SynchronizeException(String message) {
        super(message);
    }
    
    public SynchronizeException(Throwable t) {
        super(t);
    }

    
}
