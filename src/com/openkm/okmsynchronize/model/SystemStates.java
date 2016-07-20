/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.model;

/**
 *
 * @author abujosa
 */
public enum SystemStates {
    
      STOPPED           ("Application stop")    
    , RUNNING           ("Application running")
    , ERROR             ("Application error")
    , BAD_CREDENTIALS   ("Application bad credentials connect to OpenKM Server")
    , BAD_REPOSITORY    ("Application bad local repository");    
    private String desc;
    
    private SystemStates(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }    
}
