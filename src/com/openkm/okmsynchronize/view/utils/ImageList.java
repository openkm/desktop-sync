package com.openkm.okmsynchronize.view.utils;

import com.openkm.okmsynchronize.view.SynchronizeDesktopView;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;


public class ImageList {

	//private static final ImageIcon notFoundIcon = new ImageIcon(Odesktop.class.getClassLoader().getResource("com/openkm/odesktop/gui/images/notFound.png"));
    private static final ImageIcon notFoundIcon = null;
    private static final Map<String, ImageIcon> map = new HashMap<String, ImageIcon>() {
        {
            put("com.openkm.odesktop.Ologo", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/Ologo.png")));
            put("com.openkm.odesktop.connection.ok", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/connect.png")));
            put("com.openkm.odesktop.connection.error", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/disconnect.png")));
            put("com.openkm.odesktop.loader", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/loader.png")));
            put("com.openkm.notification.o", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/O.png")));
            put("com.openkm.notification.alert", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/alert.png")));
            put("com.openkm.notification.message", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/emoticon_smile.png")));
            put("com.openkm.notification.error", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/error.png")));
            put("com.openkm.notification.info", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/information.png")));
            put("com.openkm.odesktop.icon.upload", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/cloud-upload.png")));
            put("com.openkm.odesktop.icon.addList", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/outgoing.png")));
            put("com.openkm.odesktop.icon.dropList", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/outgoing-2.png")));
            put("com.openkm.odesktop.icon.dropAllList", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/reply-all.png")));
            put("com.openkm.odesktop.icon.exit", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/power.png")));
            put("com.openkm.odesktop.icon.preferences", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/settings.png")));
            put("com.openkm.odesktop.icon.search", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/search.png")));
            put("com.openkm.odesktop.icon.pause", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/pause.png")));
            put("com.openkm.odesktop.icon.synch", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/swap.png")));
            put("com.openkm.odesktop.icon.conflict", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/alert.png")));
            put("com.openkm.odesktop.icon.play", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/play.png")));
            put("com.openkm.odesktop.icon.stop", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/stop.png")));
            put("com.openkm.odesktop.icon.loader32", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/spinner.png")));
            put("com.openkm.odesktop.icon.folderAdd", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/folder-add.png")));
            put("com.openkm.odesktop.icon.fileAdd", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/folder-add.png")));
            put("com.openkm.odesktop.logo.blanc_negre", new ImageIcon(SynchronizeDesktopView.class.getClassLoader().getResource("com/openkm/okmsynchronize/view/images/logo_blanc_negre.png")));
        }
    };

    public static Image getImage(String key) {
        if(map.containsKey(key)) {
            return map.get(key).getImage();
        } else  {
            return notFoundIcon.getImage();
        }
    }

    public static ImageIcon getImageIcon(String key) {
        if(map.containsKey(key)) {
            return map.get(key);
        } else  {
            return notFoundIcon;
        }
    }
}
