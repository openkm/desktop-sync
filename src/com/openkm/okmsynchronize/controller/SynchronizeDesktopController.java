/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.controller;

import com.openkm.okmsynchronize.Constants;
import com.openkm.okmsynchronize.model.AlertManagerModel;
import com.openkm.okmsynchronize.model.ConfigurationModel;
import com.openkm.okmsynchronize.model.ContextOpenKMServer;
import com.openkm.okmsynchronize.model.LocalTreeModel;
import com.openkm.okmsynchronize.model.StateSynchronizeObject;
import com.openkm.okmsynchronize.model.SynchronizeAlert;
import com.openkm.okmsynchronize.model.SynchronizeDesktopModel;
import com.openkm.okmsynchronize.model.SynchronizedObject;
import com.openkm.okmsynchronize.model.SynchronizedObjectConflict;
import com.openkm.okmsynchronize.model.SynchronizedRepository;
import com.openkm.okmsynchronize.utils.SynchronizeLog;
import com.openkm.okmsynchronize.utils.Utils;
import com.openkm.okmsynchronize.view.Communication;
import com.openkm.okmsynchronize.view.ConfigurationView;
import com.openkm.okmsynchronize.view.ServerNodeTreeModel;
import com.openkm.okmsynchronize.view.SynchronizeAlertView;
import com.openkm.okmsynchronize.view.SynchronizeDesktopView;
import com.openkm.okmsynchronize.view.SynchronizeLogView;
import com.openkm.okmsynchronize.view.SynchronizeResolveConflictView;
import com.openkm.okmsynchronize.view.SynchronizedObjectInfoView;
import com.openkm.okmsynchronize.ws.OpenKMWSVersions;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

/**
 *
 * @author abujosa
 */
public class SynchronizeDesktopController {

    protected static final String KEY_BUNDLE = SynchronizeDesktopController.class.getName();

    protected SynchronizeLog log;

    protected SynchronizeDesktopModel model;
    protected SynchronizeDesktopView view;

    protected DoWork doWork;
    
    private String localSelectedNode = null;

    /**
     * Constructor
     */
    public SynchronizeDesktopController(SynchronizeDesktopModel model, SynchronizeDesktopView desktop) {

        this.model = model;
        this.view = desktop;

        log = model.getSynchronizeLog();

        // Add listeners to view
        view.addButtonOptionsListener(new ButtonOptions());
        view.addComboBoxContextServerListener(new ChangedServerContext());
        view.addTreeRepositoryListener(new ClickRepositoryNodeTree());
        view.addTreeServeryListener(new ClickServerNodeTree());
        view.addMenuItemServerTreeListener(new ClickMenuServerTree());
        view.addMenuItemRepositoryTreeListener(new ClickMenuRepositoryTree());
        view.addDesktopWindowListener(new DesktopWindowListener());
        view.addMenuItemTableObjectsListener(new ClickMenuObjectsTable());
        view.addButtonLogListener(new ButtonViewLog());
        view.addButtonAlertListener(new ButtonViewAlerts());
        view.addButtonStartServiceListener(new ButtonStartService());
        view.addButtonStopServiceListener(new ButtonStopService());
        view.addLabelInfoActionListener(new ClickLabelInfo());

        doWork = new DoWork();
        doWork.start();

    }

    /**
     * private class
     */
    class DoWork extends Thread {

        private boolean stop;

        public DoWork() {
            super("DoWork-DesktopController");
            stop = false;
        }

        public void setStop(boolean stop) {
            this.stop = stop;
        }

        @Override
        public void run() {
            log.info(KEY_BUNDLE + " Stating desktop handler notifycations");
            while (!stop) {
                try {
                    Thread.sleep((1 * 1000));
                    
                    // Notify error
                    view.setNotifyErrorInfo(model.haveNewError());
                    
                    // Notify alert
                    view.setNotifyAlertInfo(model.getAlertManager().isCheckAlertActive(AlertManagerModel.KeyChecks.KEY_NEW_ALERT));
                    
                    // Notify progres task bar
                    view.setNotifyTaskBar(model.getAlertManager().isCheckAlertActive(AlertManagerModel.KeyChecks.KEY_FODER_SYNCHRONIZE_RUNNING));                    
                                       
                } catch (InterruptedException ex) {
                    log.error(KEY_BUNDLE, ex);
                }
            }

            log.info(KEY_BUNDLE + " Stopping desktop handler notifycations");
        }

    }

    private void openInBrowser(String uuid) throws IOException {
        String navigator = model.getConfiguration().getKeyValue(ConfigurationModel.KEY_NAVIGATOR);
        String host = model.getConfiguration().getKeyValue(ConfigurationModel.KEY_HOST);
        Process p = Runtime.getRuntime().exec(navigator + " " + host + "?uuid=" + uuid);
    }

    private void openInRepository(String path) throws IOException {
        String explorer = model.getConfiguration().getKeyValue(ConfigurationModel.kEY_EXPLORER);
        Process p = Runtime.getRuntime().exec(explorer + " \"" + path + "\"");
    }

    /**
     * Configurate application options
     */
    class ButtonOptions implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean credentialsChanged = false;
            boolean workDirectoryChanged = false;
            
            // Init configuration change control
            model.getConfiguration().initChangeControl();

            ConfigurationView configureView = new ConfigurationView(model.getConfiguration());
            ConfigurationController controller = new ConfigurationController(model.getConfiguration(), configureView, Boolean.FALSE);
            configureView.setLocationRelativeTo(view);
            configureView.setVisible(true);

            if (model.getConfiguration().isChanged()) {                                                

                if (model.getConfiguration().isChangedKey(ConfigurationModel.KEY_WORK_DIRECTORY)) {
                    log.info(KEY_BUNDLE + " work directory changed. New path: " + model.getConfiguration().getKeyValue(ConfigurationModel.KEY_WORK_DIRECTORY));

                    // Change work directory
                    model.setWorkingDirectory(model.getConfiguration().getKeyValue(ConfigurationModel.KEY_WORK_DIRECTORY));
                    
                    // Change log 
                    boolean debug = "DEBUG".equals(model.getConfiguration().getKeyValue(ConfigurationModel.KEY_DEBUG_LEVEL));
                    model.setSynchronizeLog(new SynchronizeLog(model.getWorkingDirectory(), debug));
                    log = model.getSynchronizeLog();
                    
                    log.info(KEY_BUNDLE + " Log initialized in new work directory.");

                    // Change repository                    
                    model.changeRepository(new SynchronizedRepository(model.getWorkingDirectory()
                                                                    , model.getConfiguration().getKeyValue(ConfigurationModel.KEY_RESTRICT_EXTENSIONS)
                                                                    , model.getConfiguration().getKeyValue(ConfigurationModel.KEY_INVALID_CHARACTERS)
                                                                    , model.getCredentials()
                                                                    , model.getSynchronizeLog()
                                                                    , model.getRepositoryWatcher()));
                    log.info(KEY_BUNDLE + " Repository initialized in new work directory.");
                    
                    // Adding alert
                    SynchronizeAlert alert = new SynchronizeAlert("Work directory changed", "User changed work directory.....", SynchronizeAlert.SynchronizeAlertType.INFO);
                    model.getAlertManager().addAlert(alert);

                    workDirectoryChanged = true;
                    
                    // refhres repository tree
                    view.refreshRepositoryTree();
                }
                
                if (model.getConfiguration().isChangedKey(ConfigurationModel.KEY_HOST)
                        || model.getConfiguration().isChangedKey(ConfigurationModel.KEY_USER)
                        || model.getConfiguration().isChangedKey(ConfigurationModel.KEY_PASSWORD)
                        || model.getConfiguration().isChangedKey(ConfigurationModel.KEY_SDK_VERSION)) {

                    // Change credentials
                    model.getCredentials().refresh(model.getConfiguration().getKeyValue(ConfigurationModel.KEY_USER), model.getConfiguration().getKeyValue(ConfigurationModel.KEY_PASSWORD), model.getConfiguration().getKeyValue(ConfigurationModel.KEY_HOST), !Utils.isEmpty(model.getConfiguration().getKeyValue(ConfigurationModel.KEY_SDK_VERSION)) ? OpenKMWSVersions.valueOf(model.getConfiguration().getKeyValue(ConfigurationModel.KEY_SDK_VERSION)) : null);
                    
                    model.getRepository().setCredentials(model.getCredentials());

                    credentialsChanged = true;

                    log.info(KEY_BUNDLE + " Credentials changed." + model.getCredentials().getInfoConnection());
                    
                    // Adding alert
                    SynchronizeAlert alert = new SynchronizeAlert("Credentials changed", "User changed Credentials.....", SynchronizeAlert.SynchronizeAlertType.INFO);
                    model.getAlertManager().addAlert(alert);
                    
                    // refresh notify area
                    view.initializeNotifyArea();
                }
                
                if(!workDirectoryChanged && model.getConfiguration().isChangedKey(ConfigurationModel.KEY_RESTRICT_EXTENSIONS)) {
                    // Change restrict extensions
                    model.getRepository().setRestrictedExtensions(model.getConfiguration().getKeyValue(ConfigurationModel.KEY_RESTRICT_EXTENSIONS));
                    log.info(KEY_BUNDLE + " Changed restric extensions." + model.getConfiguration().getKeyValue(ConfigurationModel.KEY_RESTRICT_EXTENSIONS));
                }
                
                if(!workDirectoryChanged && model.getConfiguration().isChangedKey(ConfigurationModel.KEY_INVALID_CHARACTERS)) {
                     // Change invalid characters
                    model.getRepository().setRestrictedExtensions(model.getConfiguration().getKeyValue(ConfigurationModel.KEY_INVALID_CHARACTERS));
                    log.info(KEY_BUNDLE + " Changed invalid characters." + model.getConfiguration().getKeyValue(ConfigurationModel.KEY_INVALID_CHARACTERS));
                }

                if (model.getConfiguration().isChangedKey(ConfigurationModel.KEY_SYNCHRONIZE_INTERVAL) || workDirectoryChanged || credentialsChanged) {

                    // Change Synchronize timer service
                    Integer polling = Utils.isEmpty(model.getConfiguration().getKeyValue(ConfigurationModel.KEY_SYNCHRONIZE_INTERVAL)) ? 0 : Integer.parseInt(model.getConfiguration().getKeyValue(ConfigurationModel.KEY_SYNCHRONIZE_INTERVAL));
                    model.getSynchronizeService().reset(polling, model.getRepository(), model.getCredentials());

                    log.info(KEY_BUNDLE + " Synchronize timer service initialized.");
                    
                    // Adding alert
                    SynchronizeAlert alert = new SynchronizeAlert("Timer service changed", "User changed timer service.....", SynchronizeAlert.SynchronizeAlertType.INFO);
                    model.getAlertManager().addAlert(alert);
                }

                // Testing new connection
                if (model.isConnectedToServer()) {
                    view.refreshServerTree();
                    view.setEnabledButtonStopService(true);
                    view.setEnabledButtonStartService(false);
                } else {
                    view.desabledServerTree();
                    model.getSynchronizeService().stop();
                    view.setEnabledButtonStartService(true);
                    view.setEnabledButtonStopService(false);
                }
            }
            
            // Stopped configuration change control
            model.getConfiguration().stopChangeControl();
        }
    }

    class ButtonStartService implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            model.getSynchronizeService().restard();
            if (model.getSynchronizeService().isRunning()) {
                view.setEnabledButtonStopService(true);
                view.setEnabledButtonStartService(false);
            }
        }
    }

    class ButtonStopService implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            model.getSynchronizeService().stop();
            if (!model.getSynchronizeService().isRunning()) {
                view.setEnabledButtonStartService(true);
                view.setEnabledButtonStopService(false);
            }
        }
    }

    class ButtonViewLog implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            showLogView();
        }
    }

    class ButtonViewAlerts implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            showAlertView();
        }
    }

    /**
     * ComboBox events controller
     */
    // Change comboBoxServerContext    
    class ChangedServerContext implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            ContextOpenKMServer i = view.getcontextOpenKMServer();
            model.setServerContext(view.getcontextOpenKMServer());
            view.refreshServerTree();
        }
    }

    /**
     * Tree local repository events controller
     */
    class ClickRepositoryNodeTree implements MouseListener {

        @Override
        public void mousePressed(MouseEvent e) {  }

        @Override
        public void mouseReleased(MouseEvent e) {
            LocalTreeModel node = view.getSelectedLocalNode();                        
            if(node != null) {
                if(SwingUtilities.isRightMouseButton(e) && node.haveContextualMenu()) {
                    view.showPopupMenuRepositoryTree(e.getX(), e.getY());
                } else if(!SwingUtilities.isRightMouseButton(e) && node.haveAcction()) {                    
                    if("RF".equals(node.getType())) {
                        localSelectedNode = null;
                        view.refreshSynchronizedFolderNode(true);
                        view.refreshTableObjects(new ArrayList<SynchronizedObject>());                        
                    } else if("F".equals(node.getType())) {    
                        localSelectedNode = node.getUuid();
                        view.refreshTableObjects(model.getRepository().getSynchronizeDocumentsByFolder(node.getUuid()));
                    } else if("RD".equals(node.getType())) {
                        localSelectedNode = null;
                        view.refreshSynchronizedDocumentsNode(true);
                        view.refreshTableObjects(model.getRepository().getSyncronizedDocuments());                        
                    }
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }
    }

    /**
     * Tree server events controller
     */
    class ClickServerNodeTree implements MouseListener {

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            ServerNodeTreeModel node = view.getSelectedServerTreNode();

            if (node != null) {
                if (SwingUtilities.isRightMouseButton(e) && !node.isRoot()) {
                    view.showPopupMenuServerTree(node.isRoot() ? "root" : node.isFolder() ? "folder" : "document", e.getX(), e.getY());
                } else {
                    if (!node.isRoot()) {
                        List<ServerNodeTreeModel> list = new ArrayList<ServerNodeTreeModel>();
                        for (SynchronizedObject sobj : model.getChildrensServerNode(node.getUuid())) {
                            list.add(new ServerNodeTreeModel(sobj.getName(), sobj.getServerPath(), sobj.getUuid(), sobj.isFolder(), Boolean.FALSE));
                        }
                        view.refreshSelectedServerTreNode(list);
                    } 
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }
    }        
    
    /**
     * Click label Alert & label Error
     */
    class ClickLabelInfo implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            JLabel l = (JLabel) e.getSource();
            if ("New Alert".equals(l.getText())) { showAlertView(); }                
            else if ("Error".equals(l.getText())) { showLogView(); }
        }

        @Override
        public void mousePressed(MouseEvent e) {  }
        @Override
        public void mouseReleased(MouseEvent e) {  }
        @Override
        public void mouseEntered(MouseEvent e) {  }
        @Override
        public void mouseExited(MouseEvent e) {  }
    }

    /**
     * MenuItem Synchronized object table
     */
    class ClickMenuObjectsTable implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JMenuItem action = (JMenuItem) e.getSource();
            SynchronizedObject sobj = view.getSynchronizeObjectSelectedRow();

            if ("Open in repository".equals(action.getText())) {
                try {
                    openInRepository(sobj.getLocalPath());
                } catch (IOException ex) {
                    log.error(KEY_BUNDLE, ex);
                }
            } else if ("Open in browser".equals(action.getText())) {
                try {
                    openInBrowser(sobj.getUuid());
                } catch (IOException ex) {
                    log.error(KEY_BUNDLE, ex);
                }
            } else if ("Information".equals(action.getText())) {
                if (sobj != null) {
                    SynchronizedObjectInfoView infoView = new SynchronizedObjectInfoView(view, true, sobj);
                    SynchronizeObjectInfoController controller = new SynchronizeObjectInfoController(sobj, infoView);
                    infoView.setLocationRelativeTo(view);
                    infoView.setVisible(true);
                }
            } else if ("Resolve conflict".equals(action.getText())) {
                if (sobj != null) {
                    if (!StateSynchronizeObject.CONFLICT.equals(sobj.getState())) {
                        Communication.showMessage(view, "This document have no conflict ");
                    } else {
                        SynchronizedObjectConflict[] listRenamedDocuments = sobj.isPossibleToRename()?  model.getRepository().getRenamedDocuments(localSelectedNode, sobj.getLocalPath(), sobj.getName()) : null;
                        SynchronizeResolveConflictView solvedView = new SynchronizeResolveConflictView(view, sobj, listRenamedDocuments, true);
                        SynchronizeResolveConflictController solvedController = new SynchronizeResolveConflictController(model.getRepository(), sobj, solvedView, model.getCredentials(), model.getSynchronizeLog());
                        solvedView.setLocationRelativeTo(view);
                        solvedView.setVisible(true);
                        view.modifiedSelectedTableRow(sobj);
                        view.refreshTableObjects(model.getRepository().getSynchronizeDocumentsByFolder(localSelectedNode));
                    }
                }
            }
        }
    }

    /**
     * MenuItem Server Tree
     */
    class ClickMenuServerTree implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JMenuItem action = (JMenuItem) e.getSource();
            ServerNodeTreeModel node = view.getSelectedServerTreNode();

            if ("Synchronize folder".equals(action.getText())) {
                if (model.getRepository().containsSynchronizedFolder(node.getUuid())) {
                    Communication.showMessage(view, "Folder " + node.getName() + " is already synchronized.");
                } else {
                    model.getRepository().addFolder(node.getUuid(), node.getName(), node.getPath());
                }
            } else if ("Synchronize document".equals(action.getText())) {
                model.getRepository().addDocument(node.getUuid());
            } else if ("Open in browser".equals(action.getText())) {
                try {
                    openInBrowser(node.getUuid());
                } catch (IOException ex) {
                    log.error(KEY_BUNDLE, ex);
                }
            }
        }
    }

    /**
     * MenuItem Repository Tree
     */
    class ClickMenuRepositoryTree implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JMenuItem action = (JMenuItem) e.getSource();
            LocalTreeModel node = view.getSelectedLocalNode();

            if ("Open in repository".equals(action.getText())) {
                try {
                    openInRepository(node.getPath());
                } catch (IOException ex) {
                    log.error(KEY_BUNDLE, ex);
                }
            } else if ("Open in browser".equals(action.getText())) {
                try {
                    openInBrowser(node.getUuid());
                } catch (IOException ex) {
                    log.error(KEY_BUNDLE, ex);
                }
            } else if ("Remove".equals(action.getText())) {
                if ("D".equals(node.getType())) {
                    if (Communication.CONFIRM_DIALOG_OK == Communication.showConfirm(view, "Are you sure to remove the document?")) {
                        model.getRepository().removeSynchronizedDocument(node.getUuid());
                        view.refreshRepositoryTree();
                        Communication.showMessage(view, "Document removed.");
                    }
                } else {
                    if (Communication.CONFIRM_DIALOG_OK == Communication.showConfirm(view, "Are you sure to remove the folder?")) {
                        model.getRepository().removeSynchronizedFolder(node.getUuid());
                        view.refreshRepositoryTree();
                        Communication.showMessage(view, "Folder removed.");
                    }
                }
            }
        }
    }

    /**
     * Windows Listener
     */
    class DesktopWindowListener implements WindowListener {
        @Override
        public void windowClosing(WindowEvent e) {
            doWork.setStop(true);
            model.setWindowVisible(SynchronizeDesktopModel.DESKOPT_WINDOW, Boolean.FALSE);
        }
        @Override
        public void windowOpened(WindowEvent e) { }
        @Override
        public void windowClosed(WindowEvent e) { }
        @Override
        public void windowIconified(WindowEvent e) { }
        @Override
        public void windowDeiconified(WindowEvent e) { }
        @Override
        public void windowActivated(WindowEvent e) { }
        @Override
        public void windowDeactivated(WindowEvent e) { }
    }
    
    /**
     * private Method     
     */
    
    /**
     * Show alert dialog
     */
    private void showAlertView() {
        model.getAlertManager().setCheckAlert(AlertManagerModel.KeyChecks.KEY_NEW_ALERT, Boolean.FALSE);
        view.setNotifyAlertInfo(model.getAlertManager().isCheckAlertActive(AlertManagerModel.KeyChecks.KEY_NEW_ALERT));

        SynchronizeAlertView alertView = new SynchronizeAlertView(view, model.getAlertManager(), true);
        SynchronizeAlertController alertController = new SynchronizeAlertController(model.getAlertManager(), alertView);
        alertView.setLocationRelativeTo(view);
        alertView.setVisible(true);
    }
    
    /**
     * show error dialog
     */
    private void showLogView() {
        SynchronizeDesktopModel.setNewError(Boolean.FALSE);
        view.setNotifyErrorInfo(SynchronizeDesktopModel.haveNewError());

        String logPath = Utils.buildLocalFilePath(model.getWorkingDirectory(), Constants.LOG_FOLDER_NAME, Constants.LOG_FILE_NAME);
        SynchronizeLogView logView = new SynchronizeLogView(view, logPath, true);
        SynchronizeLogController logController = new SynchronizeLogController(model.getConfiguration(), logView);
        logView.setLocationRelativeTo(view);
        logView.setVisible(true);
    }

}
