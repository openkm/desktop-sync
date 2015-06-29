/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.model;

/**
 *
 * @author abujosa
 */
public enum ConflictType {
    
      RENAME_LOCK           ("RENAME_LOCK")
    , RENAME_EDIT           ("RENAME_EDIT")
    , RENAME_PERMISSIONS    ("RENAME_PERMISSIONS")
    , RENAME_NOT_EXISTS     ("RENAME_NOT_EXISTS")
    , RENAME_STOP           ("RENAME_STOP")
    , MODIFY_LOCK           ("MODIFY_LOCK")
    , MODIFY_EDIT           ("MODIFY_EDIT")
    , MODIFY_PERMISSIONS    ("MODIFY_PERMISSIONS")
    , MODIFY_CHANGE         ("MODIFY_CHANGE")
    , MODIFY_NOT_EXISTS     ("MODIFY_NOT_EXISTS")
    , DELETE_LOCK           ("DELETE_LOCK")
    , DELETE_EDIT           ("DELETE_EDIT")
    , DELETE_PERMISSIONS    ("DELETE_PERMISSIONS")
    , DELETE_CHANGE         ("DELETE_CHANGE")
    , DELETE_STOP           ("DELETE_STOP")
    , NEW_EXISTS            ("NEW_EXISTS")
    , NEW_PERMISSIONS       ("NEW_PERMISSIONS")
    , NO_CONNECTION         ("Could not connect to server.")
    , NO_CONFLICT           ("No conclict.");
    
    
    private String message;
    
    private ConflictType(String msg) {
        message = msg;
    }
    
    public String getName() {
        return name();
    }
    
    public String getMessage() {
        return message;
    }
    
}
