/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.model;

/**
 *
 * @author abujosa
 */
public enum StateSynchronizeObject {
    
     NEW        ("NEW")
   , RENAMED    ("RENAMED")
   , UPDATE     ("UPDATED")
   , MODIFIED   ("MODIFIED")
   , DELETE     ("DELETED")
   , LOCK       ("LOCK")
   , CONFLICT   ("CONFLICT");
    
    private String description;

    private StateSynchronizeObject(String description) {
        this.description = description;
    }
    
    public String getName() {
        return name();
    }

    public String getDescription() {
        return description;
    }
    
}
