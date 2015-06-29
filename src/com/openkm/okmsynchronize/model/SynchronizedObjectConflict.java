/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.model;

/**
 *
 * @author andreu
 */
public class SynchronizedObjectConflict {
    
    /* Document name. */
    private String name;
    
    /* Document version. */
    private String version;    
    
    /* Document uuid. */
    private String uuid;

    public SynchronizedObjectConflict(String name, String version, String uuid) {
        this.name = name;
        this.version = version;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return name + " (Version:" + version + ")";
    }
    
}
