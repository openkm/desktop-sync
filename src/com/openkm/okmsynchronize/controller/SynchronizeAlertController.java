/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.controller;

import com.openkm.okmsynchronize.model.SynchronizeAlert;
import com.openkm.okmsynchronize.model.AlertManagerModel;
import com.openkm.okmsynchronize.view.SynchronizeAlertView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JMenuItem;

/**
 *
 * @author abujosa
 */
public class SynchronizeAlertController {
        
    private AlertManagerModel model;
    private SynchronizeAlertView view;
    private Integer uuidShowedAlert;

    public SynchronizeAlertController(AlertManagerModel model, SynchronizeAlertView view) {
        this.model = model;
        this.view = view;
        
        this.uuidShowedAlert = null;
        
        view.addButtonCloseActionListener(new ButtonClose());
        view.addButtonRefreshActionListener(new ButtonRefresh());
        view.addComboAlertTypeActionlistener(new ChangedAlertType());
        view.addMenuItemTableAlertsListener(new PopupMenuTableAlerts());
        view.addTableAlertsListener(new TableAlerts());
    }
    
    
    // click button close dialog
    class ButtonClose implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
           view.setVisible(false);
           view.dispose();
        }        
    }
    
    // click button refresh alerts
    class ButtonRefresh implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            String selectedType = view.getAlertType();
            if ("All".equals(selectedType)) {
                view.refreshTableContent(model.getAllAlerts());
            } else {
                for (SynchronizeAlert.SynchronizeAlertType type : SynchronizeAlert.SynchronizeAlertType.values()) {
                    if (type.getText().equals(selectedType)) {
                        view.refreshTableContent(model.getAlerts(type));
                        view.showAlertInformation("");
                        uuidShowedAlert = null;
                    }
                }
            }
        }
    }
    
    // Change comboBoxAlertType    
    class ChangedAlertType implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedType = view.getAlertType();
            if ("All".equals(selectedType)) {
                view.refreshTableContent(model.getAllAlerts());
            } else {
                for (SynchronizeAlert.SynchronizeAlertType type : SynchronizeAlert.SynchronizeAlertType.values()) {
                    if (type.getText().equals(selectedType)) {
                        view.refreshTableContent(model.getAlerts(type));
                        view.showAlertInformation("");
                        uuidShowedAlert = null;                        
                    }
                }
            }
        }
    }
        
    // Popup menu table alerts
    class PopupMenuTableAlerts implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JMenuItem action = (JMenuItem) e.getSource();
            SynchronizeAlert alert = view.getSelectedAlert();
            int row = view.getRowSelectedAlert();

            if (alert != null && "Delete".equals(action.getText())) {
                model.deleteAlert(alert);
                view.deleteAlert(row);
                alert.getUuid().equals(uuidShowedAlert);
                view.showAlertInformation("");
                uuidShowedAlert = null;
            }
        }
    }
    
    //Table alerts
    class TableAlerts implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) { }

        @Override
        public void mousePressed(MouseEvent e) {
            SynchronizeAlert alert = view.getSelectedAlert();
            if (alert != null) {
                uuidShowedAlert = alert.getUuid();
                view.showAlertInformation(alert.getInformation());
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) { }

        @Override
        public void mouseEntered(MouseEvent e) { }

        @Override
        public void mouseExited(MouseEvent e) { }        
    }
    
}
