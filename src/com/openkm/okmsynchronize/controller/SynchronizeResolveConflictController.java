/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.controller;

import com.openkm.okmsynchronize.model.ConflictType;
import com.openkm.okmsynchronize.model.ServerCredentials;
import com.openkm.okmsynchronize.model.SynchronizedObject;
import com.openkm.okmsynchronize.model.SynchronizedObjectConflict;
import com.openkm.okmsynchronize.model.SynchronizedRepository;
import com.openkm.okmsynchronize.service.SynchronizeService;
import com.openkm.okmsynchronize.utils.SynchronizeException;
import com.openkm.okmsynchronize.utils.SynchronizeLog;
import com.openkm.okmsynchronize.view.Communication;
import com.openkm.okmsynchronize.view.SynchronizeResolveConflictView;
import com.openkm.okmsynchronize.ws.OpenKMWS;
import com.openkm.okmsynchronize.ws.OpenKMWSFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author abujosa
 */
public class SynchronizeResolveConflictController {

    private static final String KEY_BUNDLE = SynchronizeResolveConflictController.class.getName();

    private final static String ACTION_SELECT = "Select action";
    private final static String ACTION_UPDATE_FROM_SERVER = "Update from server";
    private final static String ACTION_UPDATE_FROM_REPOSITORY = "Update from localy";
    private final static String ACTION_RENAMED = "Rename";
    private final static String ACTION_RENAMED_UPDATE = "Rename and update";

    private SynchronizedObject model;
    private SynchronizeResolveConflictView view;
    private SynchronizedRepository repository;
    private ServerCredentials credentials;
    private SynchronizeLog log;

    public SynchronizeResolveConflictController(SynchronizedRepository repository, SynchronizedObject model, SynchronizeResolveConflictView view, ServerCredentials credentials, SynchronizeLog log) {
        this.model = model;
        this.repository = repository;
        this.credentials = credentials;
        this.log = log;
        this.view = view;

        // add components listeners
        view.addButtonCancelListener(new ButtonCancel());
        view.addButtonResolveListener(new ButtonResolve());
    }
    
    public static String[] getResolveConflictActions(ConflictType ct) {
        String[] actions = new String[]{ACTION_SELECT}; 
        
        switch(ct) {
            case RENAME_LOCK : 
                actions = new String[]{ACTION_SELECT, ACTION_UPDATE_FROM_SERVER, ACTION_RENAMED};
                break;
            case RENAME_EDIT : 
                actions = new String[]{ACTION_SELECT, ACTION_UPDATE_FROM_SERVER, ACTION_RENAMED};                
                break;
            case RENAME_PERMISSIONS :
                actions = new String[]{ACTION_SELECT, ACTION_UPDATE_FROM_SERVER, ACTION_RENAMED};
                break;
            case RENAME_NOT_EXISTS :
                actions = new String[]{ACTION_SELECT, ACTION_UPDATE_FROM_REPOSITORY};
                break;
            case RENAME_STOP : 
                actions = new String[]{ACTION_SELECT, ACTION_RENAMED, ACTION_RENAMED_UPDATE};
                break;
            case MODIFY_LOCK : 
                actions = new String[]{ACTION_SELECT, ACTION_UPDATE_FROM_SERVER, ACTION_UPDATE_FROM_REPOSITORY};
                break;
            case MODIFY_EDIT :
                actions = new String[]{ACTION_SELECT, ACTION_UPDATE_FROM_SERVER, ACTION_UPDATE_FROM_REPOSITORY};
                break;
            case MODIFY_PERMISSIONS :
                actions = new String[]{ACTION_SELECT, ACTION_UPDATE_FROM_SERVER, ACTION_UPDATE_FROM_REPOSITORY};
                break;
            case MODIFY_CHANGE :actions = new String[]{ACTION_SELECT, ACTION_UPDATE_FROM_SERVER, ACTION_UPDATE_FROM_REPOSITORY};
                break;                
            case MODIFY_NOT_EXISTS :
                actions = new String[]{ACTION_SELECT, ACTION_UPDATE_FROM_SERVER, ACTION_UPDATE_FROM_REPOSITORY};
                break;
            case DELETE_LOCK :
                actions = new String[]{ACTION_SELECT, ACTION_UPDATE_FROM_SERVER, ACTION_UPDATE_FROM_REPOSITORY};
                break;
            case DELETE_EDIT :
                actions = new String[]{ACTION_SELECT, ACTION_UPDATE_FROM_SERVER, ACTION_UPDATE_FROM_REPOSITORY};
                break;
            case DELETE_PERMISSIONS :
                actions = new String[]{ACTION_SELECT, ACTION_UPDATE_FROM_SERVER, ACTION_UPDATE_FROM_REPOSITORY};
                break;
            case DELETE_CHANGE :
                actions = new String[]{ACTION_SELECT, ACTION_UPDATE_FROM_SERVER, ACTION_UPDATE_FROM_REPOSITORY};
                break;
            case DELETE_STOP :
                actions = new String[]{ACTION_SELECT, ACTION_UPDATE_FROM_SERVER, ACTION_UPDATE_FROM_REPOSITORY};
                break;
            case NEW_EXISTS :
                actions = new String[]{ACTION_SELECT, ACTION_UPDATE_FROM_SERVER, ACTION_UPDATE_FROM_REPOSITORY};
                break;
            case NEW_PERMISSIONS :
                actions = new String[]{ACTION_SELECT, ACTION_UPDATE_FROM_SERVER, ACTION_UPDATE_FROM_REPOSITORY};
                break;
            case NO_CONNECTION :
                actions = new String[]{ACTION_SELECT, ACTION_UPDATE_FROM_SERVER, ACTION_UPDATE_FROM_REPOSITORY};
                break;
            case NO_CONFLICT :
                actions = new String[]{};
                break;
        }
        
        return actions;
    }

    class ButtonCancel implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            view.dispose();
            view.setVisible(false);
        }
    }

    class ButtonResolve implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            try {
                String action = view.getSelectedAction();
                
                OpenKMWS ws = OpenKMWSFactory.instance(credentials);
                SynchronizeService service = SynchronizeService.getInstance(ws, log);
                
                int result = 1;
                
                if (ACTION_SELECT.equals(action)) {
                    Communication.showMessage(view, "Action is not selected. Choose an action please.");
                    result = 4;
                } else if (ACTION_UPDATE_FROM_SERVER.equals(action)) {
                    result = service.updateFromServer(model);
                } else if (ACTION_UPDATE_FROM_REPOSITORY.equals(action)) {
                    result = service.updateFromRepository(model);
                } else if (ACTION_RENAMED.equals(action)) {
                    SynchronizedObjectConflict renamedDocument = view.getSelectedRenamedDocument();
                    if (renamedDocument == null) {
                        Communication.showMessage(view, "Renamed document not selected. Choose a renamed document from the list.");
                        result = 4;
                    } else {
                        result = service.renameDocument(model, renamedDocument);
                        repository.purgeSynchronizedObject(renamedDocument.getUuid(), renamedDocument.getName());
                    }
                } else if (ACTION_RENAMED_UPDATE.equals(action)) {
                    SynchronizedObjectConflict renamedDocument = view.getSelectedRenamedDocument();
                    if (renamedDocument == null) {
                        Communication.showMessage(view, "Renamed document not selected. Choose a renamed document from the list.");
                        result = 4;
                    } else {
                        result = service.renameDocumentAndUpdate(model, renamedDocument);
                        repository.purgeSynchronizedObject(renamedDocument.getUuid(), renamedDocument.getName());
                    }
                }
                
                if (result == 0) {
                    Communication.showMessage(view, "Conflict resolved");
                    view.dispose();
                    view.setVisible(false);
                } else if (result == 1) {
                    Communication.showMessage(view, "An error has ocurred. Conflict can not resolved");
                }
            } catch (SynchronizeException ex) {
                log.error(KEY_BUNDLE, ex);
                Communication.showMessage(view, "An error has ocurred. Conflict can not resolved");
            }
        }
    }              

}
