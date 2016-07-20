/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openkm.okmsynchronize.model;

import com.openkm.okmsynchronize.utils.Utils;

/**
 *
 * @author abujosa
 */
public class LocalTreeModel {
    
    private String path;
    private String name;
    private String uuid;
    private String type;  //R root | RF Root folders | RD Root Documents | F Folder
    private Boolean expanded;

    public LocalTreeModel(String path, String name, String uuid, String type) {
        this.path = path;
        this.name = name;
        this.uuid = uuid;
        this.type = type;
        this.expanded = null;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    } 

    public Boolean isExpanded() {
        return expanded != null && expanded;
    }

    public void setExpanded(Boolean expanded) {
        this.expanded = expanded;
    }        
    
    public boolean haveContextualMenu() {
        return "F".equals(type) || "D".equals(type);
    }
    
    public boolean haveAcction() {
        return "RF".equals(type) || "RD".equals(type) || "F".equals(type);
    }

    @Override
    public String toString() {
        return getName();
    }    
    
}
