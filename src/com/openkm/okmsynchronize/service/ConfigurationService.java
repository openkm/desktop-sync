package com.openkm.okmsynchronize.service;

import com.openkm.okmsynchronize.controller.ConfigurationController;
import com.openkm.okmsynchronize.model.ConfigurationModel;
import static com.openkm.okmsynchronize.model.ConfigurationModel.KEY_PASSWORD;
import com.openkm.okmsynchronize.utils.Encrypter;
import com.openkm.okmsynchronize.utils.Utils;
import com.openkm.okmsynchronize.view.ConfigurationView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe principal de Odesktop
 *
 * @author abujosab (Andreu Bujosa Bestard)
 * @version 1.0
 * @see com.openkm.okmsynchronize.service.ConfigurationService
 */
public class ConfigurationService {

    private static ConfigurationService singleton = new ConfigurationService();

    /**
     * Construeix un objecte de la classe. Aquest mètode és privat per forçar el
     * patrò singleton.
     */
    private ConfigurationService() {
        super();
    }

    /**
     * Recupera l'objecte singleton de la classe.
     */
    public static ConfigurationService getConfigurationService() {
        return singleton;
    }

    public void establirPrimeraConfiguracio(ConfigurationModel con) {
        ConfigurationView view = new ConfigurationView(con);
        ConfigurationController controller = new ConfigurationController(con, view, Boolean.TRUE);
        view.setLocationRelativeTo(view);
        view.setVisible(true);
    }

    public Map<String, String> readConfiguration(File fc) {
        Map<String, String> mc = null;

        if (fc.exists()) {

            mc = new HashMap<String, String>();
            if (Utils.isWindows()) {
                Utils.displayLocalFile(fc.getPath());
            }

            FileReader fr = null;

            try {
                fr = new FileReader(fc);
                BufferedReader br = new BufferedReader(fr);
                String linia;
                while ((linia = br.readLine()) != null) {
                    if (linia.length() > 2 && !linia.startsWith("#")) {
                        String key = linia.substring(0, linia.indexOf("=")).trim();
                        String value = linia.substring(linia.indexOf("=") + 1, linia.length()).trim();
                        mc.put(key, (KEY_PASSWORD.equals(key) && !Utils.isEmpty(value)) ? Encrypter.decryptPassword(value) : value);
                    }
                }

                if (fr != null) {
                    fr.close();
                }

            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            } finally {
                if (Utils.isWindows()) {
                    Utils.hideLocalFile(fc.getPath());
                }
            }
        } else {
            mc = null;
        }

        return mc;
    }

    public void writeMapConfiguration(File fc, Map<String, String> mc) {
        if (fc.exists() && Utils.isWindows()) {
            Utils.displayLocalFile(fc.getPath());
        }

        FileWriter fw;
        try {
            fw = new FileWriter(fc);
            PrintWriter pw = new PrintWriter(fw);

            for (String key : mc.keySet()) {

                pw.println(key + "=" + (KEY_PASSWORD.equals(key) && !Utils.isEmpty(mc.get(key)) ? Encrypter.encryptPassword(mc.get(key)) : mc.get(key)));
            }

            if (fw != null) {
                fw.close();
            }

        } catch (IOException e) {
        } finally {
            if (Utils.isWindows()) {
                Utils.hideLocalFile(fc.getPath());
            }
        }
    }
}
