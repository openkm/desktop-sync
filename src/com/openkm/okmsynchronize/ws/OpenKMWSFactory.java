package com.openkm.okmsynchronize.ws;

import com.openkm.okmsynchronize.model.ServerCredentials;
import com.openkm.okmsynchronize.utils.SynchronizeException;

/**
 * Implementació de la interície OopenkmWS per la versió Openkm-sdk4j
 *
 * @author Andreu Bujosa Bestard
 * @version 1.0
 * @see com.openkm.okmsynchronize.ws.OpenKMWSFactory
 */

public class OpenKMWSFactory {

    public static OpenKMWS instance(ServerCredentials credentials) throws SynchronizeException {

        if (credentials.isValid()) {

            switch (credentials.getVersion()) {
                case v1_0:
                    return OpenKMWS_sdk1_0.getOopenkmWS_sdk1_0(credentials.getHost()
                                                             , credentials.getUserName()
                                                             , credentials.getPassword());

                case v2_0:
                    return OpenKMWS_sdk2_0.getOopenkmWS_sdk2_0(credentials.getHost()
                                                             , credentials.getUserName()
                                                             , credentials.getPassword());

                default:
                    throw new SynchronizeException("Could not instance ws. Not Version informated.");
            }
        } else {
            return null;
        }
    }

}
