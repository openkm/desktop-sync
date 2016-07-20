/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.view;

import com.openkm.okmsynchronize.view.utils.SpringUtilities;
import com.openkm.okmsynchronize.model.ConfigurationModel;
import com.openkm.okmsynchronize.ws.OpenKMWSVersions;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

/**
 *
 * @author abujosa
 */
public class ConfigurationView extends javax.swing.JDialog {
    
    private ConfigurationModel model;
    
    public ConfigurationView(ConfigurationModel m) {
        
        this.model = m;
        
        initComponents();
        setModal(true);
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        
        setTitle("Options");
        setBounds(100, 100, 650, 500);
        getContentPane().setLayout(new BorderLayout());
        
        panel = new JPanel();
        flowLayout = (FlowLayout) panel.getLayout();
        flowLayout.setAlignment(FlowLayout.RIGHT);
        getContentPane().add(panel, BorderLayout.SOUTH);
        
        buttonAccept = new JButton("Accept");
        panel.add(buttonAccept);
        buttonCancel = new JButton("Cancel");
        panel.add(buttonCancel);
        
        panel_1 = new JPanel();        
        getContentPane().add(panel_1, BorderLayout.CENTER);
        panel_1.setLayout(new SpringLayout());
        {
            String[] labels = {"Work path:", "SDK version:", "Server:", "User:", "Pasword:", "Restricted extensions:", "Invalid characters:", "Refresh (seconds):", "Navigator:", "OS explorer:"};
            int numPairs = labels.length;
            for (int i = 0; i < numPairs; i++) {
                label = new JLabel(labels[i], JLabel.TRAILING);
                buttonVoid = new JButton("void");
                textField = new JTextField("init");
                jComboBox1 = new JComboBox(new javax.swing.DefaultComboBoxModel(OpenKMWSVersions.getOpenKMVersion()));

                if ("Work path:".equals(labels[i])) {
                    textField.setText(model.getKeyValue(ConfigurationModel.KEY_WORK_DIRECTORY));

                    buttonSearchWorkDirectory = new JButton("Find");
                    buttonSearchWorkDirectory.setVisible(true);                    

                } else if ("Server:".equals(labels[i])) {
                    textField.setText(model.getKeyValue(ConfigurationModel.KEY_HOST));

                    buttonTest = new JButton("Test");
                    buttonTest.setVisible(true);                   
                    
                } else if ("User:".equals(labels[i])) {
                    textField.setText(model.getKeyValue(ConfigurationModel.KEY_USER));
                    buttonVoid.setVisible(false);
                    
                } else if ("Pasword:".equals(labels[i])) {
                    textField.setText(model.getKeyValue(ConfigurationModel.KEY_PASSWORD));
                    buttonVoid.setVisible(false);
                    
                } else if ("Restricted extensions:".equals(labels[i])) {
                    textField.setText(model.getKeyValue(ConfigurationModel.KEY_RESTRICT_EXTENSIONS));
                    buttonVoid.setVisible(false);
                    
                } else if ("Invalid characters:".equals(labels[i])) {
                    textField.setText(model.getKeyValue(ConfigurationModel.KEY_INVALID_CHARACTERS));
                    buttonVoid.setVisible(false);
                    
                } else if ("Refresh (seconds):".equals(labels[i])) {
                    textField.setText(model.getKeyValue(ConfigurationModel.KEY_SYNCHRONIZE_INTERVAL));
                    buttonVoid.setVisible(false);
                    
                } else if ("Navigator:".equals(labels[i])) {
                    textField.setText(model.getKeyValue(ConfigurationModel.KEY_NAVIGATOR));
                    buttonVoid.setVisible(false);
                    
                } else if ("SDK version:".equals(labels[i])) {                    
                    jComboBox1.setSelectedItem(model.getKeyValue(ConfigurationModel.KEY_SDK_VERSION));
                    buttonVoid.setVisible(false);
                    
                } else if ("OS explorer:".equals(labels[i])) {
                    textField.setText(model.getKeyValue(ConfigurationModel.kEY_EXPLORER));
                    buttonVoid.setVisible(false);                    
                }

                if ("SDK version:".equals(labels[i])) {
                    label.setLabelFor(jComboBox1);
                    panel_1.add(label);
                    panel_1.add(jComboBox1);
                } else {
                    label.setLabelFor(textField);
                    panel_1.add(label);
                    panel_1.add(textField);
                }
                
                if ("Server:".equals(labels[i])) {
                    panel_1.add(buttonTest);
                } else if ("Work path:".equals(labels[i])) {
                    panel_1.add(buttonSearchWorkDirectory);
                } else {
                     panel_1.add(buttonVoid);
                }
            }
            SpringUtilities.makeCompactGrid(panel_1,
                    numPairs, 3, //rows, cols
                    6, 6, //initX, initY
                    6, 6);       //xPad, yPad


            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new BorderLayout(0, 0));
            
        }        
    }
    
    private String formatUrlHost(String url) {
        if(!url.startsWith("http://")) {
            return "http://"  + url;
        } else {
            return url;
        }
    }
    
    // Method controller
    // Buttons
    public void addButtonAcceptListener(ActionListener l) { buttonAccept.addActionListener(l); }
    public void addButtonCancelListener(ActionListener l) { buttonCancel.addActionListener(l); }
    public void addButtonSearchWorkDirectoryListener(ActionListener l) { buttonSearchWorkDirectory.addActionListener(l); }
    public void addButtonTestListener(ActionListener l) { buttonTest.addActionListener(l); }
    
    // Getter and Setter
    public void setWorkingDirectoriValue(String path) { ((JTextField) panel_1.getComponent(1)).setText(path); }    
    public String getWorkingDirectoriValue()        { return ((JTextField) panel_1.getComponent(1)).getText(); }    
    public String getSDKVersionValue()              { return (String) ((JComboBox) panel_1.getComponent(4)).getSelectedItem(); }    
    public String getHostValue()                    { return formatUrlHost(((JTextField) panel_1.getComponent(7)).getText()); }
    public String getUserValue()                    { return ((JTextField) panel_1.getComponent(10)).getText(); }
    public String getPasswordValue()                { return ((JTextField) panel_1.getComponent(13)).getText(); }
    public String getRestrictedExtensionsValue()    { return ((JTextField) panel_1.getComponent(16)).getText(); }
    public String getInvalidCharactersValue()       { return ((JTextField) panel_1.getComponent(19)).getText(); }
    public String getSynchronizeIntervalValue()     { return ((JTextField) panel_1.getComponent(22)).getText(); }    
    public String getNavigatorValue()               { return ((JTextField) panel_1.getComponent(25)).getText(); }
    public String getExplorerValue()                { return ((JTextField) panel_1.getComponent(28)).getText(); }     
    
    // Variables declaration - do not modify
    JPanel panel;
    JPanel panel_1;
    FlowLayout flowLayout;
    JButton buttonAccept;
    JButton buttonCancel;
    JButton buttonSearchWorkDirectory;
    JButton buttonTest;
    JButton buttonVoid;
    JLabel label;
    JTextField textField;
    JComboBox jComboBox1;
    
}
